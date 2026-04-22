package com.plusmobileapps.chefmate.grocery.core.snapshots

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListScreen
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.junit.Rule
import org.junit.Test

class GroceryListScreenLandscapeSnapshotTest {

    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig =
                DeviceConfig.PIXEL_6.copy(
                    screenWidth = DeviceConfig.PIXEL_6.screenHeight,
                    screenHeight = DeviceConfig.PIXEL_6.screenWidth,
                ),
            maxPercentDifference = 1.0,
        )

    @Test
    fun groceryListScreen_landscape() {
        val defaultList = GroceryListModel(id = 1L, name = "My Groceries")
        val sampleItems =
            listOf(
                GroceryListBloc.GroceryGroup(
                    category = GroceryCategory.PRODUCE,
                    items =
                        listOf(
                            GroceryItem(
                                id = 1,
                                name = "Apples",
                                quantity = "6",
                                category = GroceryCategory.PRODUCE,
                                syncStatus = SyncStatus.SYNCED,
                            ),
                            GroceryItem(
                                id = 2,
                                name = "Bananas",
                                quantity = "1 bunch",
                                category = GroceryCategory.PRODUCE,
                                isChecked = true,
                                syncStatus = SyncStatus.SYNCED,
                            ),
                        ),
                ),
                GroceryListBloc.GroceryGroup(
                    category = GroceryCategory.DAIRY,
                    items =
                        listOf(
                            GroceryItem(
                                id = 3,
                                name = "Milk",
                                quantity = "1 gallon",
                                category = GroceryCategory.DAIRY,
                                syncStatus = SyncStatus.NOT_SYNCED,
                            )
                        ),
                ),
            )
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ChefMateTheme {
                    GroceryListScreen(
                        bloc =
                            StubGroceryListBloc(
                                model =
                                    GroceryListBloc.Model(
                                        groupedItems = sampleItems,
                                        lists = listOf(defaultList),
                                        selectedList = defaultList,
                                    )
                            )
                    )
                }
            }
        }
    }
}
