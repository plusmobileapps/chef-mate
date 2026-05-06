package com.plusmobileapps.chefmate.snapshot

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import com.plusmobileapps.chefmate.cook.WhatsCookingScreen
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.junit.Rule
import org.junit.Test

class WhatsCookingScreenTest {
    @get:Rule val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val recipes =
        listOf(
            WhatsCookingBloc.Model.Item(1L, "Spaghetti Bolognese", imageUrl = null),
            WhatsCookingBloc.Model.Item(2L, "Caesar Salad", imageUrl = null),
            WhatsCookingBloc.Model.Item(3L, "Chocolate Lava Cake", imageUrl = null),
        )

    @Test
    fun whatsCookingScreen_empty() {
        paparazzi.snapshot { ChefMateTheme { WhatsCookingScreen(bloc = FakeWhatsCookingBloc()) } }
    }

    @Test
    fun whatsCookingScreen_withRecipes() {
        paparazzi.snapshot {
            ChefMateTheme {
                WhatsCookingScreen(
                    bloc = FakeWhatsCookingBloc(WhatsCookingBloc.Model(recipes = recipes))
                )
            }
        }
    }

    @Test
    fun whatsCookingScreen_selectMode() {
        paparazzi.snapshot {
            ChefMateTheme {
                WhatsCookingScreen(
                    bloc =
                        FakeWhatsCookingBloc(
                            WhatsCookingBloc.Model(
                                recipes = recipes,
                                isSelectMode = true,
                                selectedRecipeIds = setOf(1L),
                            )
                        )
                )
            }
        }
    }
}
