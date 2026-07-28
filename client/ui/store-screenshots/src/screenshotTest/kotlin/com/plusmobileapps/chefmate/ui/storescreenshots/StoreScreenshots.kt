package com.plusmobileapps.chefmate.ui.storescreenshots

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.aichat.impl.ui.previewAiChatBloc
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocStacked
import com.plusmobileapps.chefmate.grocery.core.impl.list.ui.previewGroceryListBloc
import com.plusmobileapps.chefmate.meal.core.impl.ui.previewMealPlanBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailScreen
import com.plusmobileapps.chefmate.recipe.core.detail.previewRecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.list.impl.ui.previewRecipeListBloc
import com.plusmobileapps.chefmate.toast.LocalToastService
import com.plusmobileapps.chefmate.toast.testing.FakeToastService
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/**
 * Store listing screenshots, rendered from the same public preview Blocs the regression snapshot
 * suite in `:client:ui:screenshot-test` uses. `collectStoreScreenshots` turns the rendered PNGs
 * into the Play and App Store Connect asset trees that fastlane uploads.
 *
 * **Function names are load-bearing.** `collectStoreScreenshots` parses each as
 * `<Target><NN><Scene>` — the target picks the store slot, `NN` orders the screenshot within that
 * slot, and `Scene` becomes the file name. Keep a scene at the same `NN` across all four targets so
 * both stores tell the same story in the same order.
 *
 * These are marketing assets, not regression tests: there are no committed reference images to diff
 * against, and nothing here runs on PRs. Adding a scene means adding all four wrappers.
 */
@Composable
private fun Scene(content: @Composable () -> Unit) {
    ChefMateTheme {
        CompositionLocalProvider(LocalToastService provides FakeToastService()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                content()
            }
        }
    }
}

// ── 01 · Recipe library ────────────────────────────────────────────────────

@Composable private fun Recipes() = Scene { previewRecipeListBloc.Content() }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone01Recipes() = Recipes()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet01Recipes() = Recipes()

@PreviewTest
@Preview(device = StoreDevices.IOS_PHONE)
@Composable
fun IosPhone01Recipes() = Recipes()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet01Recipes() = Recipes()

// ── 02 · Recipe detail ─────────────────────────────────────────────────────

// RecipeDetailScreen reads LocalToastService.current directly, so `Scene` must provide it for the
// screen to compose at all.
@Composable
private fun RecipeDetail() = Scene { RecipeDetailScreen(bloc = previewRecipeDetailBloc) }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone02RecipeDetail() = RecipeDetail()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet02RecipeDetail() = RecipeDetail()

@PreviewTest
@Preview(device = StoreDevices.IOS_PHONE)
@Composable
fun IosPhone02RecipeDetail() = RecipeDetail()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet02RecipeDetail() = RecipeDetail()

// ── 03 · Cook mode ─────────────────────────────────────────────────────────

@Composable private fun CookMode() = Scene { previewCookBlocStacked.Content() }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone03CookMode() = CookMode()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet03CookMode() = CookMode()

@PreviewTest
@Preview(device = StoreDevices.IOS_PHONE)
@Composable
fun IosPhone03CookMode() = CookMode()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet03CookMode() = CookMode()

// ── 04 · Grocery list ──────────────────────────────────────────────────────

@Composable private fun GroceryList() = Scene { previewGroceryListBloc.Content() }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone04GroceryList() = GroceryList()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet04GroceryList() = GroceryList()

@PreviewTest
@Preview(device = StoreDevices.IOS_PHONE)
@Composable
fun IosPhone04GroceryList() = GroceryList()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet04GroceryList() = GroceryList()

// ── 05 · Meal plan ─────────────────────────────────────────────────────────

@Composable private fun MealPlan() = Scene { previewMealPlanBloc.Content() }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone05MealPlan() = MealPlan()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet05MealPlan() = MealPlan()

@PreviewTest
@Preview(device = StoreDevices.IOS_PHONE)
@Composable
fun IosPhone05MealPlan() = MealPlan()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet05MealPlan() = MealPlan()

// ── 06 · AI chat ───────────────────────────────────────────────────────────

@Composable private fun AiChat() = Scene { previewAiChatBloc.Content() }

@PreviewTest
@Preview(device = StoreDevices.PLAY_PHONE)
@Composable
fun PlayPhone06AiChat() = AiChat()

@PreviewTest
@Preview(device = StoreDevices.PLAY_TABLET)
@Composable
fun PlayTablet06AiChat() = AiChat()

@PreviewTest @Preview(device = StoreDevices.IOS_PHONE) @Composable fun IosPhone06AiChat() = AiChat()

@PreviewTest
@Preview(device = StoreDevices.IOS_TABLET)
@Composable
fun IosTablet06AiChat() = AiChat()
