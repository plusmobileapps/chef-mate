@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.importer.impl

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipePhotoStorage
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookRepository
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class ImportRecipesViewModelTest {

    private val recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val repository = FakeRecipeRepository(recipes)
    private val books =
        MutableStateFlow(
            listOf(
                RecipeBook.Sample.copy(id = 1L, name = "My Recipes", isDefault = true),
                RecipeBook.Sample.copy(id = 2L, name = "Weeknight Dinners", isDefault = false),
            )
        )
    private val recipeBookRepository = FakeRecipeBookRepository(books)
    private val photoStorage = FakeRecipePhotoStorage()
    private val dispatcher = UnconfinedTestDispatcher()

    private fun createViewModel() =
        ImportRecipesViewModel(
            mainContext = dispatcher,
            ioContext = dispatcher,
            repository = repository,
            recipeBookRepository = recipeBookRepository,
            photoStorage = photoStorage,
        )

    @Test
    fun When_archive_selected_Then_parsed_recipes_are_shown_for_review() {
        val vm = createViewModel()

        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        val review = vm.state.value.phase.shouldBeInstanceOf<Phase.Review>()
        review.recipes.size shouldBe 2
        review.recipes.map { it.title } shouldBe listOf("Garlic Noodles", "Lentil Curry")
        review.recipes.all { it.selected } shouldBe true
    }

    @Test
    fun When_archive_has_no_recipes_Then_error_is_shown() {
        val vm = createViewModel()

        vm.onArchiveSelected(zip("plain.html" to "<html></html>".encodeToByteArray()), "empty.zip")

        vm.state.value.phase.shouldBeInstanceOf<Phase.Error>()
    }

    @Test
    fun When_recipe_deselected_Then_only_selected_recipes_are_imported() {
        photoStorage.nextResult = { "https://cdn.example.com/garlic.jpg" }
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")
        val review = vm.state.value.phase.shouldBeInstanceOf<Phase.Review>()
        val lentil = review.recipes.first { it.title == "Lentil Curry" }

        vm.onRecipeToggled(lentil.id)
        vm.onImportClicked()

        vm.state.value.phase.shouldBeInstanceOf<Phase.Done>().importedCount shouldBe 1
        recipes.value.map { it.title } shouldBe listOf("Garlic Noodles")
        photoStorage.uploads.size shouldBe 1
        recipes.value.single().imageUrl shouldBe "https://cdn.example.com/garlic.jpg"
    }

    @Test
    fun When_all_recipes_imported_Then_repository_persists_them_all() {
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        vm.onImportClicked()

        vm.state.value.phase.shouldBeInstanceOf<Phase.Done>().importedCount shouldBe 2
        recipes.value.size shouldBe 2
    }

    @Test
    fun Initially_the_active_book_is_selected_as_the_import_target() {
        val vm = createViewModel()

        vm.selectedBookIds.value shouldBe setOf(1L)
        vm.recipeBooks.value.map { it.id } shouldBe listOf(1L, 2L)
    }

    @Test
    fun When_imported_Then_recipes_are_filed_under_the_selected_books() {
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        // Also file under the second book in addition to the default active one.
        vm.onToggleBook(2L)
        vm.onImportClicked()

        vm.state.value.phase.shouldBeInstanceOf<Phase.Done>()
        recipes.value.forEach { it.recipeBookIds shouldBe setOf(1L, 2L) }
    }

    @Test
    fun When_active_book_deselected_Then_recipes_file_under_remaining_book() {
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        vm.onToggleBook(2L)
        vm.onToggleBook(1L)
        vm.selectedBookIds.value.shouldContainExactlyInAnyOrder(2L)
        vm.onImportClicked()

        recipes.value.forEach { it.recipeBookIds shouldBe setOf(2L) }
    }

    @Test
    fun When_no_book_selected_Then_import_is_blocked() {
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        // Deselect the pre-selected active book, leaving no import target.
        vm.onToggleBook(1L)
        vm.selectedBookIds.value shouldBe emptySet()
        vm.onImportClicked()

        vm.state.value.phase.shouldBeInstanceOf<Phase.Review>()
        recipes.value shouldBe emptyList()
    }

    @Test
    fun When_start_over_clicked_Then_state_resets_to_empty() {
        val vm = createViewModel()
        vm.onArchiveSelected(twoRecipeArchive(), "export.zip")

        vm.onStartOver()

        vm.state.value.phase shouldBe Phase.Empty
    }

    private fun twoRecipeArchive(): ByteArray =
        zip(
            "Garlic Noodles.html" to
                recipeHtml(name = "Garlic Noodles", imageSrc = "Images/garlic.jpg")
                    .encodeToByteArray(),
            "Images/garlic.jpg" to byteArrayOf(1, 2, 3),
            "Lentil Curry.html" to
                recipeHtml(name = "Lentil Curry", imageSrc = null).encodeToByteArray(),
        )

    private fun recipeHtml(name: String, imageSrc: String?): String {
        val img = imageSrc?.let { "<img itemprop=\"image\" src=\"$it\" />" }.orEmpty()
        return """
            <html><body>
              <div itemscope itemtype="http://schema.org/Recipe">
                <h1 itemprop="name">$name</h1>
                $img
                <ul><li itemprop="recipeIngredient">salt</li></ul>
                <div itemprop="recipeInstructions"><p>Cook it.</p></div>
              </div>
            </body></html>
            """
            .trimIndent()
    }

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, bytes) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }
}
