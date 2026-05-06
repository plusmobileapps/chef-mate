package com.plusmobileapps.chefmate.snapshot

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.cook.CookModeScreen
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.junit.Rule
import org.junit.Test

class CookModeScreenTest {
    @get:Rule val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val testRecipe =
        Recipe.Empty.copy(
            id = 1L,
            title = "Spaghetti Bolognese",
            ingredients =
                "500g spaghetti\n1 onion, diced\n2 cloves garlic\n500g ground beef\n400g tomato sauce\n2 tbsp olive oil",
            directions =
                "Bring a large pot of salted water to a boil and cook spaghetti until al dente.\n" +
                    "Heat olive oil in a skillet over medium heat. Sauté onion until translucent.\n" +
                    "Add garlic and cook for one minute.\n" +
                    "Add ground beef and brown, breaking it apart as it cooks.\n" +
                    "Stir in tomato sauce and simmer for 20 minutes.\n" +
                    "Drain pasta and toss with sauce. Serve immediately.",
        )

    @Test
    fun cookModeScreen_loading() {
        paparazzi.snapshot {
            ChefMateTheme {
                CookModeScreen(bloc = FakeCookModeBloc(CookModeBloc.Model(isLoading = true)))
            }
        }
    }

    @Test
    fun cookModeScreen_empty() {
        paparazzi.snapshot {
            ChefMateTheme {
                CookModeScreen(
                    bloc =
                        FakeCookModeBloc(CookModeBloc.Model(isLoading = false, activeRecipe = null))
                )
            }
        }
    }

    @Test
    fun cookModeScreen_stackedLayout_light() {
        paparazzi.snapshot {
            ChefMateTheme {
                CookModeScreen(
                    bloc =
                        FakeCookModeBloc(
                            CookModeBloc.Model(
                                isLoading = false,
                                activeRecipe = testRecipe,
                                layoutMode = CookModeBloc.LayoutMode.Stacked,
                            )
                        )
                )
            }
        }
    }

    @Test
    fun cookModeScreen_stackedLayout_dark() {
        paparazzi.snapshot {
            ChefMateTheme(darkTheme = true) {
                CookModeScreen(
                    bloc =
                        FakeCookModeBloc(
                            CookModeBloc.Model(
                                isLoading = false,
                                activeRecipe = testRecipe,
                                layoutMode = CookModeBloc.LayoutMode.Stacked,
                            )
                        )
                )
            }
        }
    }

    @Test
    fun cookModeScreen_splitLayout() {
        paparazzi.snapshot {
            ChefMateTheme {
                CookModeScreen(
                    bloc =
                        FakeCookModeBloc(
                            CookModeBloc.Model(
                                isLoading = false,
                                activeRecipe = testRecipe,
                                layoutMode = CookModeBloc.LayoutMode.Split,
                            )
                        )
                )
            }
        }
    }

    @Test
    fun cookModeScreen_multipleSessions() {
        paparazzi.snapshot {
            ChefMateTheme {
                CookModeScreen(
                    bloc =
                        FakeCookModeBloc(
                            CookModeBloc.Model(
                                isLoading = false,
                                activeRecipe = testRecipe,
                                activeSessions =
                                    listOf(
                                        CookModeBloc.Model.Chip(
                                            1L,
                                            "Spaghetti Bolognese",
                                            isActive = true,
                                        ),
                                        CookModeBloc.Model.Chip(
                                            2L,
                                            "Caesar Salad",
                                            isActive = false,
                                        ),
                                        CookModeBloc.Model.Chip(3L, "Tiramisu", isActive = false),
                                    ),
                            )
                        )
                )
            }
        }
    }
}
