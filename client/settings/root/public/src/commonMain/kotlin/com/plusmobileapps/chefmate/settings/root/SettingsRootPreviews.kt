@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.settings.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.bottomnav.previewBottomNavOrderBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.previewBottomNavOrderDirtyBloc
import com.plusmobileapps.chefmate.settings.previewAppSettingsBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

private data object PreviewMainConfig

private data object PreviewDetailsConfig

private fun settingsRootBloc(
    panels: Panels<PreviewMainConfig, PreviewDetailsConfig, Nothing>,
    detailsBloc: SettingsRootBloc.DetailsChild? = null,
): SettingsRootBloc =
    object : SettingsRootBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val panels:
            Value<
                ChildPanels<*, SettingsRootBloc.MainChild, *, SettingsRootBloc.DetailsChild, *, *>
            > =
            MutableValue<
                ChildPanels<
                    PreviewMainConfig,
                    SettingsRootBloc.MainChild,
                    PreviewDetailsConfig,
                    SettingsRootBloc.DetailsChild,
                    Nothing,
                    Nothing,
                >
            >(
                ChildPanels(
                    main =
                        Child.Created(
                            configuration = panels.main,
                            instance =
                                SettingsRootBloc.MainChild.AppSettings(previewAppSettingsBloc),
                        ),
                    details =
                        panels.details?.let { config ->
                            Child.Created(configuration = config, instance = detailsBloc!!)
                        },
                    extra = null,
                    mode = panels.mode,
                )
            )
    }

/** Master only, no detail selected — the centered-on-load state on tablet. */
val previewSettingsRootMasterOnly: SettingsRootBloc =
    settingsRootBloc(panels = Panels(main = PreviewMainConfig))

/** Master + Bottom-Nav-Order detail visible — the post-selection state on tablet. */
val previewSettingsRootWithDetails: SettingsRootBloc =
    settingsRootBloc(
        panels =
            Panels(
                main = PreviewMainConfig,
                details = PreviewDetailsConfig,
                mode = ChildPanelsMode.DUAL,
            ),
        detailsBloc = SettingsRootBloc.DetailsChild.BottomNavOrder(previewBottomNavOrderBloc),
    )

/** Master + detail in a "dirty" state — the Save FAB is visible inside the detail pane. */
val previewSettingsRootWithDirtyDetails: SettingsRootBloc =
    settingsRootBloc(
        panels =
            Panels(
                main = PreviewMainConfig,
                details = PreviewDetailsConfig,
                mode = ChildPanelsMode.DUAL,
            ),
        detailsBloc = SettingsRootBloc.DetailsChild.BottomNavOrder(previewBottomNavOrderDirtyBloc),
    )

@Preview(showBackground = true)
@Composable
internal fun SettingsRootMasterOnlyPreview() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
internal fun SettingsRootTabletWithDetailsPreview() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootWithDetails) }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
internal fun SettingsRootTabletMasterOnlyPreview() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}
