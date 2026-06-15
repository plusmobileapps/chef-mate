@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import app.cash.turbine.test
import com.plusmobileapps.chefmate.recipe.core.addmeal.RecipePickerBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class RecipePickerBlocTest {
    private val context = TestBlocContext.create()
    private val output = TestConsumer<RecipePickerBloc.Output>()

    private val books =
        listOf(
            RecipeBook.Sample.copy(id = 1L, name = "Weeknight Dinners"),
            RecipeBook.Sample.copy(id = 2L, name = "Holiday Baking"),
        )
    private val recipes =
        listOf(
            recipe(id = 10L, title = "Pasta Carbonara", bookIds = setOf(1L)),
            recipe(id = 20L, title = "Gingerbread", bookIds = setOf(2L, 1L)),
            recipe(id = 30L, title = "Mystery Stew", bookIds = setOf(99L)),
        )

    private val recipeRepository = FakeRecipeRepository(MutableStateFlow(recipes))
    private val recipeBookRepository = FakeRecipeBookRepository(MutableStateFlow(books))

    private fun createBloc(): RecipePickerBlocImpl =
        RecipePickerBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                RecipePickerViewModel(
                    mainContext = context.mainContext,
                    recipeRepository = recipeRepository,
                    recipeBookRepository = recipeBookRepository,
                )
            },
        )

    @Test
    fun WHEN_loaded_THEN_each_result_lists_its_recipe_book_names() = runTest {
        createBloc().state.test {
            val state = awaitItem()
            state.isLoading shouldBe false
            state.recipes.first { it.id == 10L }.bookNames shouldBe listOf("Weeknight Dinners")
            // Multiple books are sorted alphabetically.
            state.recipes.first { it.id == 20L }.bookNames shouldBe
                listOf("Holiday Baking", "Weeknight Dinners")
        }
    }

    @Test
    fun WHEN_recipe_book_is_unknown_THEN_no_book_name_is_shown() = runTest {
        createBloc().state.test {
            // Recipe 30 references a book id that has no matching book — it should drop silently
            // rather than crash or show a blank label.
            awaitItem().recipes.first { it.id == 30L }.bookNames shouldBe emptyList()
        }
    }

    @Test
    fun WHEN_search_query_set_THEN_results_filter_by_title_across_all_books() = runTest {
        val bloc = createBloc()
        bloc.state.test {
            awaitItem().recipes.map { it.id } shouldBe listOf(10L, 20L, 30L)

            bloc.onSearchQueryChanged("ginger")

            awaitItem().recipes.map { it.id } shouldBe listOf(20L)
        }
    }

    private fun recipe(id: Long, title: String, bookIds: Set<Long>): Recipe =
        Recipe.Empty.copy(
            id = id,
            title = title,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
            recipeBookIds = bookIds,
        )
}
