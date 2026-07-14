package com.plusmobileapps.chefmate.recipe.core.impl.share

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.core.share.PublicRecipeBloc
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AssistedInject
class PublicRecipeViewModel(
    @Assisted private val remoteId: String,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
) : ViewModel(mainContext) {

    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()

    private val _state = MutableStateFlow<PublicRecipeBloc.Model>(PublicRecipeBloc.Model.Loading)
    val state: StateFlow<PublicRecipeBloc.Model> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _state.value = PublicRecipeBloc.Model.Loading
        scope.launch {
            // Owner/collaborator who already has this recipe: skip the read-only preview and open
            // the real detail screen instead of offering to save a duplicate.
            repository.getRecipeByRemoteId(remoteId)?.let { local ->
                _output.send(Output.OpenRecipe(local.id))
                return@launch
            }
            repository
                .fetchPublicRecipe(remoteId)
                .onSuccess { recipe ->
                    _state.value = PublicRecipeBloc.Model.Loaded(recipe = recipe)
                }
                .onFailure { error ->
                    // A missing/private recipe is a definitive "not found"; anything else (network,
                    // serialization) is treated as a retryable transient failure.
                    _state.value =
                        if (error is NoSuchElementException) PublicRecipeBloc.Model.NotFound
                        else PublicRecipeBloc.Model.Offline
                }
        }
    }

    fun onRetry() = load()

    fun onSave() {
        val loaded = _state.value as? PublicRecipeBloc.Model.Loaded ?: return
        if (loaded.isSaving) return
        _state.value = loaded.copy(isSaving = true)
        scope.launch {
            // Reset local/remote identity so createRecipe files a brand-new recipe owned by the
            // current user (owner is stamped on push) rather than mutating the shared original.
            val copy =
                loaded.recipe.copy(
                    id = -1,
                    remoteId = null,
                    isPublic = false,
                    recipeBookIds = emptySet(),
                )
            val saved = repository.createRecipe(copy)
            _output.send(Output.OpenRecipe(saved.id))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _output.close()
    }

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(remoteId: String): PublicRecipeViewModel
    }
}
