@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.edit

import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeCategoryRepository
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipePhotoStorage
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class EditRecipeViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)
    private val categoryRepository = FakeCategoryRepository()
    private val photoStorage = FakeRecipePhotoStorage()
    private val mainContext = UnconfinedTestDispatcher()

    private fun createViewModel(
        recipeId: Long? = null,
        extractedRecipe: ExtractedRecipeData? = null,
    ) =
        EditRecipeViewModel(
            recipeId = recipeId,
            extractedRecipe = extractedRecipe,
            mainContext = mainContext,
            repository = repository,
            categoryRepository = categoryRepository,
            photoStorage = photoStorage,
        )

    @Test
    fun When_no_recipe_id_or_extracted_data_Then_all_fields_start_empty() {
        val vm = createViewModel()
        vm.title.value shouldBe ""
        vm.description.value shouldBe ""
        vm.ingredients.value shouldBe ""
        vm.directions.value shouldBe ""
        vm.imageUrl.value shouldBe ""
        vm.sourceUrl.value shouldBe ""
        vm.servings.value shouldBe ""
        vm.prepTime.value shouldBe ""
        vm.cookTime.value shouldBe ""
        vm.totalTime.value shouldBe ""
        vm.calories.value shouldBe ""
        vm.starRating.value shouldBe null
    }

    @Test
    fun When_extracted_recipe_provided_Then_all_fields_seeded() {
        val extracted =
            ExtractedRecipeData(
                title = "Extracted Title",
                description = "Extracted description",
                ingredients = listOf("1 cup flour", "2 eggs"),
                directions = listOf("Step 1", "Step 2"),
                imageUrl = "https://example.com/image.jpg",
                sourceUrl = "https://example.com/recipe",
                servings = 4,
                prepTime = 10,
                cookTime = 20,
                totalTime = 30,
                calories = 250,
            )

        val vm = createViewModel(extractedRecipe = extracted)

        vm.title.value shouldBe "Extracted Title"
        vm.description.value shouldBe "Extracted description"
        vm.ingredients.value shouldBe "1 cup flour\n2 eggs"
        vm.directions.value shouldBe "Step 1\nStep 2"
        vm.imageUrl.value shouldBe "https://example.com/image.jpg"
        vm.sourceUrl.value shouldBe "https://example.com/recipe"
        vm.servings.value shouldBe "4"
        vm.prepTime.value shouldBe "10"
        vm.cookTime.value shouldBe "20"
        vm.totalTime.value shouldBe "30"
        vm.calories.value shouldBe "250"
        // starRating is not part of extracted data; remains null.
        vm.starRating.value shouldBe null
    }

    @Test
    fun When_extracted_recipe_has_null_optional_fields_Then_those_fields_are_empty() {
        val extracted =
            ExtractedRecipeData(
                title = "Sparse",
                description = null,
                ingredients = emptyList(),
                directions = emptyList(),
                imageUrl = null,
                sourceUrl = "https://example.com",
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )

        val vm = createViewModel(extractedRecipe = extracted)

        vm.title.value shouldBe "Sparse"
        vm.description.value shouldBe ""
        vm.ingredients.value shouldBe ""
        vm.directions.value shouldBe ""
        vm.imageUrl.value shouldBe ""
        vm.servings.value shouldBe ""
        vm.prepTime.value shouldBe ""
        vm.cookTime.value shouldBe ""
        vm.totalTime.value shouldBe ""
        vm.calories.value shouldBe ""
    }

    @Test
    fun When_recipe_id_provided_Then_extracted_data_is_ignored() = runTest {
        val existing =
            Recipe(
                id = 7,
                title = "Existing",
                description = "Existing desc",
                ingredients = "existing ingredients",
                directions = "existing directions",
                imageUrl = null,
                sourceUrl = null,
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
                starRating = null,
                isFavorite = false,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        recipes.value = listOf(existing)

        val extracted =
            ExtractedRecipeData(
                title = "Extracted",
                description = null,
                ingredients = listOf("ignored"),
                directions = listOf("ignored"),
                imageUrl = null,
                sourceUrl = "https://example.com",
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )

        val vm = createViewModel(recipeId = 7, extractedRecipe = extracted)

        // Existing recipe wins over extracted seed.
        vm.title.value shouldBe "Existing"
        vm.ingredients.value shouldBe "existing ingredients"
    }

    @Test
    fun When_save_with_extracted_seed_Then_repository_creates_new_recipe() = runTest {
        val extracted =
            ExtractedRecipeData(
                title = "From Web",
                description = null,
                ingredients = listOf("flour"),
                directions = listOf("mix"),
                imageUrl = null,
                sourceUrl = "https://example.com/recipe",
                servings = 4,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
            )
        val vm = createViewModel(extractedRecipe = extracted)

        vm.save()
        val saved = vm.output.first()
        saved.shouldBeFinished()
        recipes.value.size shouldBe 1
        recipes.value.first().title shouldBe "From Web"
        recipes.value.first().ingredients shouldBe "flour"
        recipes.value.first().sourceUrl shouldBe "https://example.com/recipe"
    }

    @Test
    fun When_no_recipe_loaded_Then_categories_starts_empty() {
        val vm = createViewModel()
        vm.categories.value shouldBe emptySet()
    }

    @Test
    fun When_recipe_with_category_loaded_Then_categories_seeded() = runTest {
        val breakfastCategory =
            Category(id = 1L, name = "Breakfast", builtinId = BuiltinCategory.BREAKFAST.id)
        val existing =
            Recipe(
                id = 11,
                title = "Existing",
                description = null,
                ingredients = "",
                directions = "",
                imageUrl = null,
                sourceUrl = null,
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
                starRating = null,
                isFavorite = false,
                categories = setOf(breakfastCategory),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        recipes.value = listOf(existing)

        val vm = createViewModel(recipeId = 11)

        vm.categories.value shouldBe setOf(breakfastCategory)
    }

    @Test
    fun When_preset_attached_and_saved_Then_persisted_on_recipe() = runTest {
        val vm = createViewModel()
        vm.updateTitle("Stack of Pancakes")
        vm.attachBuiltin(BuiltinCategory.BREAKFAST)

        vm.save()
        vm.output.first().shouldBeFinished()

        recipes.value.size shouldBe 1
        recipes.value.first().categories.singleOrNull()?.builtinId shouldBe
            BuiltinCategory.BREAKFAST.id
    }

    @Test
    fun When_attaching_same_preset_twice_Then_only_one_row_is_attached() = runTest {
        val vm = createViewModel()
        vm.updateTitle("Bagels")
        vm.attachBuiltin(BuiltinCategory.BREAKFAST)
        vm.attachBuiltin(BuiltinCategory.BREAKFAST)

        vm.save()
        vm.output.first().shouldBeFinished()

        // materializeBuiltin returns the existing row on the second call, so the Set ignores
        // the duplicate; downstream join sync would otherwise create stale rows.
        recipes.value.first().categories.size shouldBe 1
    }

    @Test
    fun When_user_category_created_Then_attached_to_recipe() = runTest {
        val vm = createViewModel()
        vm.updateTitle("Family Favorites")

        vm.createUserCategoryAndAttach("Weeknight")

        vm.categories.value.singleOrNull()?.name shouldBe "Weeknight"
    }

    @Test
    fun When_attached_user_category_renamed_Then_in_memory_set_reflects_new_name() = runTest {
        val vm = createViewModel()
        vm.createUserCategoryAndAttach("Weknight") // typo
        val created = vm.categories.value.single()

        vm.renameUserCategory(created.id, "Weeknight")

        vm.categories.value.single().name shouldBe "Weeknight"
    }

    @Test
    fun When_unattached_user_category_renamed_Then_selection_set_untouched() = runTest {
        val vm = createViewModel()
        // Seed a user category without attaching it.
        val created = categoryRepository.createUserCategory("Slow Cooker")

        vm.renameUserCategory(created.id, "Slow Cooker (Crockpot)")

        vm.categories.value shouldBe emptySet()
    }

    @Test
    fun When_categories_changed_on_existing_recipe_Then_back_shows_discard_dialog() = runTest {
        seedExistingRecipe(categories = emptySet())
        val vm = createViewModel(recipeId = 99)

        vm.attachBuiltin(BuiltinCategory.BREAKFAST)
        vm.tryToClose()

        vm.state.value.showDiscardChangesDialog shouldBe true
    }

    @Test
    fun When_attached_category_renamed_Then_back_shows_discard_dialog() = runTest {
        val existing = categoryRepository.createUserCategory("Weknight")
        seedExistingRecipe(categories = setOf(existing))
        val vm = createViewModel(recipeId = 99)

        vm.renameUserCategory(existing.id, "Weeknight")
        vm.tryToClose()

        vm.state.value.showDiscardChangesDialog shouldBe true
    }

    @Test
    fun When_attached_category_deleted_Then_back_shows_discard_dialog() = runTest {
        val existing = categoryRepository.createUserCategory("Slow Cooker")
        seedExistingRecipe(categories = setOf(existing))
        val vm = createViewModel(recipeId = 99)

        vm.deleteUserCategory(existing.id)
        vm.tryToClose()

        vm.state.value.showDiscardChangesDialog shouldBe true
    }

    @Test
    fun When_no_changes_made_Then_back_skips_discard_dialog() = runTest {
        seedExistingRecipe(categories = emptySet())
        val vm = createViewModel(recipeId = 99)

        vm.tryToClose()

        vm.state.value.showDiscardChangesDialog shouldBe false
    }

    private fun seedExistingRecipe(categories: Set<Category>) {
        recipes.value =
            listOf(
                Recipe(
                    id = 99,
                    title = "Existing",
                    description = null,
                    ingredients = "",
                    directions = "",
                    imageUrl = null,
                    sourceUrl = null,
                    servings = null,
                    prepTime = null,
                    cookTime = null,
                    totalTime = null,
                    calories = null,
                    starRating = null,
                    isFavorite = false,
                    categories = categories,
                    createdAt = Instant.DISTANT_PAST,
                    updatedAt = Instant.DISTANT_PAST,
                )
            )
    }

    @Test
    fun When_attached_user_category_deleted_Then_removed_from_selection() = runTest {
        val vm = createViewModel()
        vm.createUserCategoryAndAttach("Weeknight")
        val created = vm.categories.value.single()

        vm.deleteUserCategory(created.id)

        vm.categories.value shouldBe emptySet()
    }

    @Test
    fun When_category_detached_Then_no_longer_attached() = runTest {
        val dinnerCategory =
            Category(id = 1L, name = "Dinner", builtinId = BuiltinCategory.DINNER.id)
        val existing =
            Recipe(
                id = 12,
                title = "Existing",
                description = null,
                ingredients = "",
                directions = "",
                imageUrl = null,
                sourceUrl = null,
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
                starRating = null,
                isFavorite = false,
                categories = setOf(dinnerCategory),
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
        recipes.value = listOf(existing)

        val vm = createViewModel(recipeId = 12)
        vm.detachCategory(dinnerCategory)

        vm.save()
        vm.output.first().shouldBeFinished()

        recipes.value.first().categories shouldBe emptySet()
    }

    @Test
    fun When_photo_picked_Then_bytes_are_held_and_no_upload_happens() {
        val vm = createViewModel()

        vm.setPendingPhoto(bytes = byteArrayOf(1, 2, 3), fileExtension = "jpg")

        vm.pendingPhotoBytes.value?.toList() shouldBe listOf<Byte>(1, 2, 3)
        vm.imageUrl.value shouldBe ""
        photoStorage.uploads.size shouldBe 0
    }

    @Test
    fun When_save_with_pending_photo_Then_photo_is_uploaded_before_recipe_save() = runTest {
        photoStorage.nextResult = { "https://cdn.example.com/photo.jpg" }
        val vm = createViewModel()
        vm.updateTitle("With photo")
        vm.setPendingPhoto(bytes = byteArrayOf(7, 7, 7), fileExtension = "png")

        vm.save()
        val output = vm.output.first()

        output.shouldBeFinished()
        photoStorage.uploads.size shouldBe 1
        photoStorage.uploads.first().fileExtension shouldBe "png"
        recipes.value.single().imageUrl shouldBe "https://cdn.example.com/photo.jpg"
        vm.pendingPhotoBytes.value shouldBe null
        vm.state.value.uploadError shouldBe null
    }

    @Test
    fun When_save_upload_fails_Then_recipe_is_not_saved_and_error_is_surfaced() = runTest {
        val failure = RuntimeException("network down")
        photoStorage.nextResult = { throw failure }
        val vm = createViewModel()
        vm.updateTitle("Will not save")
        vm.setPendingPhoto(bytes = byteArrayOf(0), fileExtension = "jpg")

        vm.save()

        recipes.value shouldBe emptyList()
        vm.state.value.isSaving shouldBe false
        vm.state.value.uploadError shouldBe failure
        vm.pendingPhotoBytes.value?.toList() shouldBe listOf<Byte>(0)

        vm.dismissUploadError()
        vm.state.value.uploadError shouldBe null
    }

    @Test
    fun When_only_photo_changed_Then_close_prompts_discard_dialog() {
        val vm = createViewModel()
        vm.setPendingPhoto(bytes = byteArrayOf(9), fileExtension = "jpg")

        vm.tryToClose()

        vm.state.value.showDiscardChangesDialog shouldBe true
    }

    private fun EditRecipeViewModel.Output.shouldBeFinished() {
        check(this is EditRecipeViewModel.Output.Finished)
    }
}
