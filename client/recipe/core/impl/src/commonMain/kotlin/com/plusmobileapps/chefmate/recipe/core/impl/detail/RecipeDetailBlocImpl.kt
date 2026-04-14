package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.browser.BrowserBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc.Output
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.util.DateTimeUtil
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@AssistedInject
class RecipeDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: RecipeDetailViewModel.Factory,
    private val dateTimeUtil: DateTimeUtil,
    private val addToGroceryList: AddRecipeToGroceryListBloc.Factory,
    private val browserBlocFactory: BrowserBloc.Factory,
) : RecipeDetailBloc,
    BlocContext by context {
    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): RecipeDetailBlocImpl
    }

    private val scope = createScope()

    private val viewModel: RecipeDetailViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory.create(recipeId)
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

    override fun onSourceUrlClicked(url: String) {
        sheetNavigation.activate(SheetConfig.BrowserLauncher(url))
    }

    override fun onDismissSheet() {
        sheetNavigation.dismiss()
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
            is SheetConfig.BrowserLauncher ->
                RecipeDetailBloc.Sheet.BrowserLauncher(
                    bloc =
                        browserBlocFactory
                            .create(
                                context = context,
                                output = { browserOutput ->
                                    when (browserOutput) {
                                        is BrowserBloc.Output.RecipeExtracted -> sheetNavigation.dismiss()
                                    }
                                },
                            ).also { bloc ->
                                bloc.onUrlChanged(config.url)
                                bloc.onNavigate()
                            },
                )
        }

    @Serializable
    sealed class SheetConfig {
        data class AddToGroceryList(
            val recipeId: Long,
        ) : SheetConfig()

        data class BrowserLauncher(
            val url: String,
        ) : SheetConfig()
    }
}

@ContributesTo(AppScope::class)
interface RecipeDetailBlocBindingModule {
    @Provides
    fun provideRecipeDetailBlocFactory(factory: RecipeDetailBlocImpl.ManagedFactory): RecipeDetailBloc.Factory =
        object : RecipeDetailBloc.Factory {
            override fun create(
                context: BlocContext,
                recipeId: Long,
                output: Consumer<Output>,
            ): RecipeDetailBloc = factory.create(context, recipeId, output)
        }
}
