package com.plusmobileapps.chefmate.family.data.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.database.FamilyMemberQueries
import com.plusmobileapps.chefmate.database.FamilyQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.family.data.Family
import com.plusmobileapps.chefmate.family.data.FamilyInvite
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.family.data.FamilyMemberStatus
import com.plusmobileapps.chefmate.family.data.FamilyRepository
import com.plusmobileapps.chefmate.family.data.FamilyRole
import com.plusmobileapps.chefmate.family.data.remote.FamilyRemoteDataSource
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyCollaborator
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Reads are served from the local cache so the Family screen renders offline; writes go
 * remote-first and throw, because "one family per user" can only be arbitrated by the database. See
 * [FamilyRepository] and the comments in `Family.sq`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FamilyRepositoryImpl(
    private val familyQueries: FamilyQueries,
    private val memberQueries: FamilyMemberQueries,
    @IO private val ioContext: CoroutineContext,
    private val dateTimeUtil: DateTimeUtil,
    private val remote: FamilyRemoteDataSource,
    private val authRepository: AuthenticationRepository,
) : FamilyRepository {

    private val scope = CoroutineScope(ioContext + SupervisorJob())
    private val syncMutex = Mutex()

    private val pendingInvitesState = MutableStateFlow<List<FamilyInvite>>(emptyList())

    private var realtimeJob: Job? = null
    private var realtimeUserId: String? = null

    private val currentUser: ChefMateUser?
        get() = (authRepository.state.value as? AuthState.Authenticated)?.user

    private val cachedFamily: Flow<com.plusmobileapps.chefmate.database.Family?> =
        familyQueries.getCurrent().asFlow().mapToOneOrNull(ioContext)

    override val family: StateFlow<Family?> =
        combine(cachedFamily, authRepository.state) { row, authState ->
                val userId = (authState as? AuthState.Authenticated)?.user?.userId
                row?.let {
                    Family(
                        id = it.id,
                        remoteId = it.remoteId,
                        name = it.name,
                        // ownerId is null only for rows written before the owner was known; treat
                        // that as "not owner" so admin actions stay hidden rather than failing
                        // against RLS.
                        isOwnedByCurrentUser = userId != null && it.ownerId == userId,
                    )
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch {
            authRepository.state.collect { state ->
                if (state is AuthState.Authenticated) {
                    syncWithRemote()
                    startRealtimeSync(state.user.userId)
                } else {
                    stopRealtimeSync()
                    clearLocalData()
                }
            }
        }
    }

    /**
     * Subscribes to remote family changes so an invite accepted, a member removed, or a rename done
     * on another device lands here without waiting for the next sign-in. Emissions are debounced to
     * coalesce bursts and each one re-runs the full reconcile.
     */
    @OptIn(FlowPreview::class)
    private fun startRealtimeSync(userId: String) {
        if (realtimeUserId == userId && realtimeJob?.isActive == true) return
        stopRealtimeSync()
        realtimeUserId = userId
        realtimeJob = scope.launch {
            remote
                .observeChanges()
                .debounce(REALTIME_DEBOUNCE_MS)
                .retry { cause ->
                    // Keep the subscription alive across transient failures, but let structured
                    // cancellation (sign-out / scope teardown) stop the loop.
                    if (cause is CancellationException) throw cause
                    delay(REALTIME_RETRY_DELAY_MS)
                    true
                }
                .catch {}
                .collect { syncWithRemote() }
        }
    }

    private fun stopRealtimeSync() {
        realtimeJob?.cancel()
        realtimeJob = null
        realtimeUserId = null
    }

    override fun members(): Flow<List<FamilyMember>> = cachedFamily.flatMapLatest { row ->
        if (row == null) {
            flowOf(emptyList())
        } else {
            memberQueries.getByFamilyId(row.id).asFlow().mapToList(ioContext).map { members ->
                members.map { member ->
                    FamilyMember(
                        id = member.remoteId,
                        email = member.userEmail,
                        role = FamilyRole.fromWire(member.role),
                        status = FamilyMemberStatus.fromWire(member.status),
                        name = member.displayName,
                        isOwner = member.isOwner != 0L,
                        avatarUrl = member.avatarUrl,
                    )
                }
            }
        }
    }

    override fun pendingInvites(): Flow<List<FamilyInvite>> = pendingInvitesState

    override suspend fun createFamily(name: String) {
        val user = requireUser()
        // Cheap local guard so the common case fails fast with a clear error; the database's
        // partial
        // unique index is still the real enforcement, caught below for the racy case.
        if (family.value != null) throw AlreadyInFamilyException()
        translatingConflict { remote.createFamily(name.trim(), user.userId) }
        syncWithRemote()
    }

    override suspend fun renameFamily(name: String) {
        val remoteId = requireFamily().remoteId
        remote.renameFamily(remoteId, name.trim())
        syncWithRemote()
    }

    override suspend fun invite(email: String) {
        val user = requireUser()
        val remoteId = requireFamily().remoteId
        // Normalise the address so it always matches the invitee's (lowercased) account email.
        remote.invite(
            familyRemoteId = remoteId,
            email = email.trim().lowercase(),
            invitedBy = user.userId,
        )
        syncWithRemote()
    }

    override suspend fun removeMember(memberId: String) {
        remote.deleteMember(memberId)
        syncWithRemote()
    }

    override suspend fun leaveFamily() {
        val user = requireUser()
        val remoteId = requireFamily().remoteId
        remote.leaveFamily(familyRemoteId = remoteId, userId = user.userId)
        // Access is gone the moment the member row is deleted, so drop the local copy rather than
        // waiting for the next reconcile to notice it's unreadable.
        clearCache()
        syncWithRemote()
    }

    override suspend fun deleteFamily() {
        val remoteId = requireFamily().remoteId
        remote.deleteFamily(remoteId)
        clearCache()
        syncWithRemote()
    }

    override suspend fun acceptInvite(memberId: String) {
        val user = requireUser()
        if (family.value != null) throw AlreadyInFamilyException()
        translatingConflict { remote.acceptInvite(memberId = memberId, userId = user.userId) }
        syncWithRemote()
    }

    override suspend fun declineInvite(memberId: String) {
        // Mark the invite rejected rather than deleting the row, so the owner can see it was turned
        // down. Access requires status = 'accepted' either way.
        remote.rejectInvite(memberId)
        syncWithRemote()
    }

    override suspend fun refresh() {
        syncWithRemote()
    }

    override suspend fun clearLocalData() {
        clearCache()
        pendingInvitesState.value = emptyList()
    }

    private suspend fun clearCache() =
        withContext(ioContext) {
            // Delete members explicitly rather than relying on the FK cascade — foreign-key
            // enforcement is a per-connection pragma and isn't guaranteed on every driver.
            memberQueries.deleteAll()
            familyQueries.deleteAll()
        }

    /**
     * Reconciles the local cache with the server: the caller's family (or its absence), its member
     * list, and any invites addressed to them. Swallows failures — the cache simply stays as it was
     * until the next realtime emission or manual refresh.
     */
    private suspend fun syncWithRemote() = syncMutex.withLock {
        if (currentUser == null) return@withLock
        try {
            val remoteFamily = remote.fetchCurrentFamily()
            val remoteFamilyId = remoteFamily?.id

            if (remoteFamily == null || remoteFamilyId == null) {
                // Not in a family — either never joined, or removed / left on another device.
                clearCache()
            } else {
                val now = dateTimeUtil.now.toString()
                withContext(ioContext) {
                    familyQueries.upsert(
                        remoteId = remoteFamilyId,
                        name = remoteFamily.name,
                        ownerId = remoteFamily.ownerId,
                        createdAt = remoteFamily.createdAt ?: now,
                        updatedAt = remoteFamily.updatedAt ?: now,
                    )
                }
                val members = remote.fetchMembers(remoteFamilyId)
                withContext(ioContext) {
                    val localId =
                        familyQueries.getByRemoteId(remoteFamilyId).executeAsOneOrNull()?.id
                    if (localId != null) {
                        // The RPC returns the full membership every time, so replace wholesale
                        // rather than diffing — removals then can't linger in the cache.
                        memberQueries.transaction {
                            memberQueries.deleteByFamilyId(localId)
                            members.forEach { memberQueries.insert(it, localId) }
                        }
                    }
                }
            }

            pendingInvitesState.value =
                remote.fetchPendingInvites().map {
                    FamilyInvite(memberId = it.memberId, familyName = it.familyName)
                }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}
    }

    private fun FamilyMemberQueries.insert(member: RemoteFamilyCollaborator, familyLocalId: Long) =
        insert(
            familyLocalId = familyLocalId,
            remoteId = member.memberId,
            userId = null,
            userEmail = member.email,
            role = member.role,
            status = member.status,
            isOwner = if (member.isOwner) 1L else 0L,
            displayName = member.name,
            avatarUrl = member.avatarUrl,
        )

    private fun requireUser(): ChefMateUser = currentUser ?: error("Not signed in")

    private fun requireFamily(): Family = family.value ?: error("Not in a family")

    /**
     * Runs [block], rethrowing a unique-constraint violation on the "one accepted family per user"
     * index as [AlreadyInFamilyException] so the UI can tell the user to leave their current family
     * rather than showing a raw Postgres error. Postgres reports it as SQLSTATE 23505.
     */
    private suspend fun <T> translatingConflict(block: suspend () -> T): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val message = e.message.orEmpty()
            if (message.contains(ONE_FAMILY_INDEX) || message.contains(UNIQUE_VIOLATION)) {
                throw AlreadyInFamilyException()
            }
            throw e
        }

    private companion object {
        const val REALTIME_DEBOUNCE_MS = 300L
        const val REALTIME_RETRY_DELAY_MS = 5_000L
        const val ONE_FAMILY_INDEX = "idx_fm_one_accepted_family_per_user"
        const val UNIQUE_VIOLATION = "23505"
    }
}
