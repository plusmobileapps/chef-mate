package com.plusmobileapps.chefmate.family.data.testing

import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.family.data.Family
import com.plusmobileapps.chefmate.family.data.FamilyInvite
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.family.data.FamilyMemberStatus
import com.plusmobileapps.chefmate.family.data.FamilyRepository
import com.plusmobileapps.chefmate.family.data.FamilyRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory [FamilyRepository] for tests and previews. The backing [MutableStateFlow]s are public
 * so a test can seed state directly or assert on the result of a call.
 */
class FakeFamilyRepository(
    private val familyState: MutableStateFlow<Family?> = MutableStateFlow(null),
    private val membersState: MutableStateFlow<List<FamilyMember>> = MutableStateFlow(emptyList()),
    private val invitesState: MutableStateFlow<List<FamilyInvite>> = MutableStateFlow(emptyList()),
) : FamilyRepository {

    /** Set to have the next mutating call throw, exercising error paths. */
    var errorToThrow: Exception? = null

    var refreshCount: Int = 0
        private set

    var clearLocalDataCount: Int = 0
        private set

    val invitedEmails: MutableList<String> = mutableListOf()
    val removedMemberIds: MutableList<String> = mutableListOf()
    val acceptedInviteIds: MutableList<String> = mutableListOf()
    val declinedInviteIds: MutableList<String> = mutableListOf()

    override val family: StateFlow<Family?> = familyState

    override fun members(): Flow<List<FamilyMember>> = membersState

    override fun pendingInvites(): Flow<List<FamilyInvite>> = invitesState

    override suspend fun createFamily(name: String) {
        throwIfConfigured()
        if (familyState.value != null) throw AlreadyInFamilyException()
        familyState.value =
            Family(id = 1L, remoteId = "family-1", name = name, isOwnedByCurrentUser = true)
        membersState.value =
            listOf(
                FamilyMember(
                    id = null,
                    email = "owner@example.com",
                    role = FamilyRole.OWNER,
                    status = FamilyMemberStatus.ACCEPTED,
                    isOwner = true,
                )
            )
    }

    override suspend fun renameFamily(name: String) {
        throwIfConfigured()
        familyState.value = familyState.value?.copy(name = name)
    }

    override suspend fun invite(email: String) {
        throwIfConfigured()
        val normalized = email.trim().lowercase()
        invitedEmails += normalized
        membersState.value =
            membersState.value +
                FamilyMember(
                    id = "member-${membersState.value.size + 1}",
                    email = normalized,
                    role = FamilyRole.MEMBER,
                    status = FamilyMemberStatus.PENDING,
                )
    }

    override suspend fun removeMember(memberId: String) {
        throwIfConfigured()
        removedMemberIds += memberId
        membersState.value = membersState.value.filterNot { it.id == memberId }
    }

    override suspend fun leaveFamily() {
        throwIfConfigured()
        familyState.value = null
        membersState.value = emptyList()
    }

    override suspend fun deleteFamily() {
        throwIfConfigured()
        familyState.value = null
        membersState.value = emptyList()
    }

    override suspend fun acceptInvite(memberId: String) {
        throwIfConfigured()
        if (familyState.value != null) throw AlreadyInFamilyException()
        acceptedInviteIds += memberId
        val invite = invitesState.value.firstOrNull { it.memberId == memberId }
        invitesState.value = invitesState.value.filterNot { it.memberId == memberId }
        if (invite != null) {
            familyState.value =
                Family(
                    id = 1L,
                    remoteId = "family-1",
                    name = invite.familyName,
                    isOwnedByCurrentUser = false,
                )
        }
    }

    override suspend fun declineInvite(memberId: String) {
        throwIfConfigured()
        declinedInviteIds += memberId
        invitesState.value = invitesState.value.filterNot { it.memberId == memberId }
    }

    override suspend fun refresh() {
        refreshCount++
    }

    override suspend fun clearLocalData() {
        clearLocalDataCount++
        familyState.value = null
        membersState.value = emptyList()
        invitesState.value = emptyList()
    }

    private fun throwIfConfigured() {
        errorToThrow?.let { throw it }
    }
}
