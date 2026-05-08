package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun bottomNavOrderBloc(model: BottomNavOrderBloc.Model): BottomNavOrderBloc =
    object : BottomNavOrderBloc {
        override val state = MutableStateFlow(model)

        override fun onMove(from: Int, to: Int) = Unit

        override fun onSave() = Unit

        override fun onBack() = Unit
    }

val previewBottomNavOrderBloc: BottomNavOrderBloc = bottomNavOrderBloc(BottomNavOrderBloc.Model())

/**
 * A reordered + dirty state: SETTINGS has been moved to the top so
 * [BottomNavOrderBloc.Model.hasUnsavedChanges] resolves to `true` and the Save FAB is visible.
 */
val previewBottomNavOrderDirtyBloc: BottomNavOrderBloc =
    bottomNavOrderBloc(
        BottomNavOrderBloc.Model(
            editedOrder =
                listOf(
                    BottomNavBloc.Tab.SETTINGS,
                    BottomNavBloc.Tab.RECIPES,
                    BottomNavBloc.Tab.GROCERIES,
                    BottomNavBloc.Tab.MEALS,
                    BottomNavBloc.Tab.BROWSER,
                ),
            persistedOrder = DEFAULT_TAB_ORDER,
        )
    )

@Preview(showBackground = true)
@Composable
internal fun BottomNavOrderScreenPreview() {
    ChefMateTheme { BottomNavOrderScreen(bloc = previewBottomNavOrderBloc) }
}

@Preview(showBackground = true)
@Composable
internal fun BottomNavOrderScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) { BottomNavOrderScreen(bloc = previewBottomNavOrderBloc) }
}

@Preview(showBackground = true)
@Composable
internal fun BottomNavOrderScreenDirtyPreview() {
    ChefMateTheme { BottomNavOrderScreen(bloc = previewBottomNavOrderDirtyBloc) }
}
