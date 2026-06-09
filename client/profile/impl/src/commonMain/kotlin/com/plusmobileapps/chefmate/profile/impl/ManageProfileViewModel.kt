package com.plusmobileapps.chefmate.profile.impl

import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_save_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.text.asTextData
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class ManageProfileViewModel(
    @Main mainContext: CoroutineContext,
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<Model> = _state.asStateFlow()

    private val _outputs = Channel<Output>(Channel.BUFFERED)
    val outputs: Flow<Output> = _outputs.receiveAsFlow()

    private fun initialState(): Model {
        val user = (authenticationRepository.state.value as? AuthState.Authenticated)?.user
        return Model(
            displayName = user?.userName.orEmpty(),
            email = user?.userEmail.orEmpty(),
        )
    }

    fun setDisplayName(displayName: String) {
        _state.update { it.copy(displayName = displayName, saveError = null) }
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(isSaving = true, saveError = null) }
        scope.launch {
            authenticationRepository
                .updateProfile(displayName = current.displayName.trim())
                .onSuccess { _outputs.send(Output.Saved) }
                .onFailure {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveError = Res.string.manage_profile_save_error.asTextData(),
                        )
                    }
                }
        }
    }

    sealed interface Output {
        data object Saved : Output
    }
}
