package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc.Output
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.util.DateTimeUtil
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeDetailBloc.Factory::class,
)
class RecipeDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: (Long) -> RecipeDetailViewModel,
    private val dateTimeUtil: DateTimeUtil,
    private val addToGroceryList: AddRecipeToGroceryListBloc.Factory,
) : RecipeDetailBloc,
    BlocContext by context {
    private val scope = createScope()

    private val viewModel: RecipeDetailViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory(recipeId)
        }

    init {
        scope.launch {
            viewModel.output.collect { viewModelOutput ->
                when (viewModelOutput) {
                    RecipeDetailViewModel.Output.RecipeDeleted -> output.onNext(Output.Finished)
                }
            }
        }
    }

    private val sheetNavigation = SlotNavigation<SheetConfig>()
    private val sheetRouter =
        childSlot(
            source = sheetNavigation,
            serializer = SheetConfig.serializer(),
            key = "RecipeDetailBloc_Sheet",
            childFactory = ::createSheet,
        )

    override val childSlot: Value<ChildSlot<*, RecipeDetailBloc.Sheet>> = sheetRouter

    override val state: StateFlow<RecipeDetailBloc.Model> =
        viewModel.state.mapState {
            RecipeDetailBloc.Model(
                isLoading = it.isLoading,
                isDeleting = it.isDeleting,
                showDeleteConfirmationDialog = it.showDeleteConfirmationDialog,
                recipe = it.recipe,
                createdAt =
                    FixedString(
                        dateTimeUtil.formatDateTime(
                            instant = it.recipe.createdAt,
                        ),
                    ),
                updatedAt =
                    FixedString(
                        dateTimeUtil.formatDateTime(
                            instant = it.recipe.updatedAt,
                        ),
                    ),
            )
        }

    override fun onEditClicked() {
        output.onNext(Output.EditRecipe(recipeId))
    }

    override fun onDeleteClicked() {
        viewModel.showDeleteConfirmationDialog()
    }

    override fun onDeleteConfirmed() {
        viewModel.confirmDelete()
    }

    override fun onDeleteDismissed() {
        viewModel.dismissDeleteConfirmationDialog()
    }

    override fun onFavoriteToggled() {
        viewModel.toggleFavorite()
    }

    override fun onAddToGroceryListClicked() {
        sheetNavigation.activate(SheetConfig.AddToGroceryList(recipeId))
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }

    private fun createSheet(
        config: SheetConfig,
        context: BlocContext,
    ): RecipeDetailBloc.Sheet =
        when (config) {
            is SheetConfig.AddToGroceryList ->
                RecipeDetailBloc.Sheet.AddToGroceryList(
                    bloc =
                        addToGroceryList.create(
                            context = context,
                            recipeId = config.recipeId,
                            output = { output ->
                                when (output) {
                                    AddRecipeToGroceryListBloc.Output.Finished -> sheetNavigation.dismiss()
                                }
                            },
                        ),
                )
        }

    @Serializable
    sealed class SheetConfig {
        data class AddToGroceryList(
            val recipeId: Long,
        ) : SheetConfig()
    }
}
