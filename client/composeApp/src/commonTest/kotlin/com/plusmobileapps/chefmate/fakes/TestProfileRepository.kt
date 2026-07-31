package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.profile.data.ProfileRepository
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.profile.data.impl.SupabaseProfileRepository
import com.plusmobileapps.chefmate.profile.data.testing.FakeProfileRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Replaces [SupabaseProfileRepository] in the test graph, which would otherwise need a real
 * [io.github.jan.supabase.SupabaseClient]. Mirrors [TestAuthenticationRepository].
 */
@SingleIn(AppScope::class)
@Inject
@ContributesBinding(scope = AppScope::class, replaces = [SupabaseProfileRepository::class])
class TestProfileRepository(private val fake: FakeProfileRepository = FakeProfileRepository()) :
    ProfileRepository by fake {

    init {
        // Line the fake up with the default authenticated test user, so "my profile" resolves the
        // same way it would in the app.
        fake.currentUserId = FakeAuthenticationRepository.fakeUser().userId
    }

    fun addProfile(profile: SocialProfile) = fake.addProfile(profile)

    fun profileFor(handle: String): SocialProfile? = fake.profileFor(handle)

    /** The default user's own profile, for tests that need them to already have one. */
    fun givenOwnProfile(handle: String = "testchef"): SocialProfile =
        SocialProfile(
                id = fake.currentUserId,
                handle = handle,
                displayName = "Test Chef",
                bio = "I test recipes.",
                avatarUrl = null,
            )
            .also { fake.addProfile(it) }
}
