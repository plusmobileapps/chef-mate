@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.share

import com.plusmobileapps.chefmate.recipe.core.share.PublicRecipeBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

class PublicRecipeBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<PublicRecipeBloc.Output>()
    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)

    private val publicRecipe =
        Recipe(
            id = -1,
            title = "Shared Soup",
            description = null,
            ingredients = "water\nsalt",
            directions = "boil",
            imageUrl = null,
            sourceUrl = null,
            servings = 2,
            prepTime = null,
            cookTime = null,
            totalTime = null,
            calories = null,
            starRating = null,
            remoteId = "remote-1",
            isPublic = true,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )

    private fun createBloc(remoteId: String = "remote-1"): PublicRecipeBlocImpl {
        val viewModelFactory = PublicRecipeViewModel.Factory { id ->
            PublicRecipeViewModel(
                remoteId = id,
                mainContext = context.mainContext,
                repository = repository,
            )
        }
        return PublicRecipeBlocImpl(
            context = context,
            remoteId = remoteId,
            output = output,
            viewModelFactory = viewModelFactory,
        )
    }

    @Test
    fun When_public_recipe_found_Then_state_is_loaded() {
        repository.publicRecipes["remote-1"] = publicRecipe
        val bloc = createBloc()
        val model = bloc.state.value
        model.shouldBeInstanceOf<PublicRecipeBloc.Model.Loaded>()
        model.recipe.title shouldBe "Shared Soup"
    }

    @Test
    fun When_public_recipe_missing_Then_state_is_not_found() {
        val bloc = createBloc(remoteId = "does-not-exist")
        bloc.state.value shouldBe PublicRecipeBloc.Model.NotFound
    }

    @Test
    fun When_recipe_already_owned_locally_Then_opens_it_without_preview() {
        recipes.value = listOf(publicRecipe.copy(id = 5L))
        val bloc = createBloc()
        output.lastValue shouldBe PublicRecipeBloc.Output.OpenRecipe(5L)
    }

    @Test
    fun When_save_clicked_Then_copy_created_and_opened() {
        repository.publicRecipes["remote-1"] = publicRecipe
        val bloc = createBloc()
        bloc.onSaveClicked()
        // FakeRecipeRepository.createRecipe returns the recipe unchanged (id -1 here); the point is
        // that a copy was created and an OpenRecipe output emitted.
        output.lastValue.shouldBeInstanceOf<PublicRecipeBloc.Output.OpenRecipe>()
        recipes.value.any { it.title == "Shared Soup" } shouldBe true
    }
}
