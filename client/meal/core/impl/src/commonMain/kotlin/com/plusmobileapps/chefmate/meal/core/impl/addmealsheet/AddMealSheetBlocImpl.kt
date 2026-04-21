package com.plusmobileapps.chefmate.meal.core.impl.addmealsheet

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.meal.core.addmealsheet.AddMealSheetBloc
import com.plusmobileapps.chefmate.meal.core.addmealsheet.AddMealSheetBloc.Output
import com.plusmobileapps.chefmate.meal.core.recipepicker.RecipePickerBloc
import com.plusmobileapps.chefmate.recipe.core.addmeal.AddToMealPlanBloc
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.serialization.Serializable

@AssistedInject
class AddMealSheetBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val recipePickerFactory: RecipePickerBloc.Factory,
    private val addToMealPlanFactory: AddToMealPlanBloc.Factory,
) : AddMealSheetBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): AddMealSheetBlocImpl
    }

    private val navigation = StackNavigation<Configuration>()
    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.RecipePicker) },
            handleBackButton = true,
            key = "AddMealSheetRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, AddMealSheetBloc.Child>> = stack

    override fun onBackClicked() {
        if (stack.value.backStack.isNotEmpty()) {
            navigation.pop()
        } else {
            output.onNext(Output.Finished)
        }
    }

    private fun createChild(config: Configuration, context: BlocContext): AddMealSheetBloc.Child =
        when (config) {
            Configuration.RecipePicker ->
                AddMealSheetBloc.Child.RecipePicker(
                    bloc =
                        recipePickerFactory.create(
                            context = context,
                            output = { pickerOutput ->
                                when (pickerOutput) {
                                    is RecipePickerBloc.Output.RecipeSelected ->
                                        navigation.pushNew(
                                            Configuration.MealPlanForm(pickerOutput.recipeId)
                                        )
                                }
                            },
                        )
                )
            is Configuration.MealPlanForm ->
                AddMealSheetBloc.Child.MealPlanForm(
                    bloc =
                        addToMealPlanFactory.create(
                            context = context,
                            recipeId = config.recipeId,
                            output = { mealPlanOutput ->
                                when (mealPlanOutput) {
                                    AddToMealPlanBloc.Output.Finished ->
                                        output.onNext(Output.Finished)
                                }
                            },
                        )
                )
        }

    @Serializable
    sealed class Configuration {
        @Serializable data object RecipePicker : Configuration()

        @Serializable data class MealPlanForm(val recipeId: Long) : Configuration()
    }
}

@ContributesTo(AppScope::class)
interface AddMealSheetBlocBindingModule {
    @Provides
    fun provideAddMealSheetBlocFactory(
        factory: AddMealSheetBlocImpl.ManagedFactory
    ): AddMealSheetBloc.Factory = AddMealSheetBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
