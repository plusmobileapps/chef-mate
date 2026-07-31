package com.plusmobileapps.chefmate.profile.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.profile.ProfileBloc.Model
import com.plusmobileapps.chefmate.profile.ProfileBloc.Props
import com.plusmobileapps.chefmate.profile.data.ProfileRepository
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@AssistedInject
class ProfileViewModel(
    @Main mainContext: CoroutineContext,
    @Assisted private val props: Props,
    private val profileRepository: ProfileRepository,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthenticationRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow<Model>(Model.Loading)
    val state: StateFlow<Model> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() {
        _state.value = Model.Loading
        load()
    }

    private fun load() {
        scope.launch {
            val handle = props.handle
            val profileResult =
                if (handle == null) {
                    profileRepository.getMyProfile()
                } else {
                    profileRepository.getProfileByHandle(handle)
                }

            profileResult.fold(
                onSuccess = { profile ->
                    when {
                        profile != null -> loadRecipes(profile)
                        // Only your own profile can be "not created yet" — for a handle that came
                        // from a link, a missing row means the profile genuinely doesn't exist.
                        handle == null -> _state.value = Model.NoProfile
                        else -> _state.value = Model.NotFound
                    }
                },
                // No local mirror of profiles, so any failure (offline, server error, signed out)
                // lands here. Offline is the actionable one, and retry covers the rest.
                onFailure = { _state.value = Model.Offline },
            )
        }
    }

    private suspend fun loadRecipes(profile: SocialProfile) {
        val recipes =
            recipeRepository.fetchPublishedRecipes(profile.id).getOrElse {
                // Surface the failure whole rather than rendering the header alone: a profile that
                // silently appears to have published nothing would be a lie.
                _state.value = Model.Offline
                return
            }
        _state.value =
            Model.Loaded(
                profile = profile,
                recipes = recipes.toImmutableList(),
                isOwnProfile = profile.id == currentUserId(),
            )
    }

    private fun currentUserId(): String? =
        (authRepository.state.value as? AuthState.Authenticated)?.user?.userId

    /** The loaded profile's handle, or null when there's nothing to share yet. */
    fun loadedHandleOrNull(): String? = (_state.value as? Model.Loaded)?.profile?.handle

    @AssistedFactory
    fun interface Factory {
        fun create(props: Props): ProfileViewModel
    }
}
