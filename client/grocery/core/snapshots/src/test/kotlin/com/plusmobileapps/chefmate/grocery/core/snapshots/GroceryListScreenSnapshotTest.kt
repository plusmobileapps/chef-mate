package com.plusmobileapps.chefmate.grocery.core.snapshots

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryFilter
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc.GroceryGroup
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListScreen
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.junit.Rule
import org.junit.Test

class GroceryListScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6, maxPercentDifference = 1.0)

    private val defaultList = GroceryListModel(id = 1L, name = "My Groceries")
    private val secondList =
        GroceryListModel(id = 2L, name = "Party Supplies", syncStatus = SyncStatus.SYNCED)

    private val sampleItems =
        listOf(
            GroceryGroup(
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
            GroceryGroup(
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
            GroceryGroup(
                category = GroceryCategory.BAKERY,
                items =
                    listOf(
                        GroceryItem(
                            id = 4,
                            name = "Sourdough Bread",
                            category = GroceryCategory.BAKERY,
                            syncStatus = SyncStatus.SYNCED,
                        )
                    ),
            ),
        )

    @Test
    fun groceryListScreen_default() {
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

    @Test
    fun groceryListScreen_empty() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ChefMateTheme {
                    GroceryListScreen(
                        bloc =
                            StubGroceryListBloc(
                                model =
                                    GroceryListBloc.Model(
                                        lists = listOf(defaultList),
                                        selectedList = defaultList,
                                    )
                            )
                    )
                }
            }
        }
    }

    @Test
    fun groceryListScreen_syncing() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ChefMateTheme {
                    GroceryListScreen(
                        bloc =
                            StubGroceryListBloc(
                                model =
                                    GroceryListBloc.Model(
                                        groupedItems = sampleItems,
                                        isSyncing = true,
                                        lists = listOf(defaultList),
                                        selectedList = defaultList,
                                    )
                            )
                    )
                }
            }
        }
    }

    @Test
    fun groceryListScreen_purchasedFilter() {
        val purchasedOnly =
            listOf(
                GroceryGroup(
                    category = GroceryCategory.PRODUCE,
                    items =
                        listOf(
                            GroceryItem(
                                id = 2,
                                name = "Bananas",
                                quantity = "1 bunch",
                                category = GroceryCategory.PRODUCE,
                                isChecked = true,
                                syncStatus = SyncStatus.SYNCED,
                            )
                        ),
                )
            )
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ChefMateTheme {
                    GroceryListScreen(
                        bloc =
                            StubGroceryListBloc(
                                model =
                                    GroceryListBloc.Model(
                                        groupedItems = purchasedOnly,
                                        filter = GroceryFilter.PURCHASED,
                                        lists = listOf(defaultList),
                                        selectedList = defaultList,
                                    )
                            )
                    )
                }
            }
        }
    }

    @Test
    fun groceryListScreen_multipleLists() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ChefMateTheme {
                    GroceryListScreen(
                        bloc =
                            StubGroceryListBloc(
                                model =
                                    GroceryListBloc.Model(
                                        groupedItems = sampleItems,
                                        lists = listOf(defaultList, secondList),
                                        selectedList = defaultList,
                                    )
                            )
                    )
                }
            }
        }
    }
}
