@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery

import com.plusmobileapps.chefmate.database.GroceryQueries
import com.plusmobileapps.chefmate.testing.suspendTest
import dev.mokkery.mock
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class GroceryRepositoryTest :
    FunSpec({
        val queries = mock<GroceryQueries>()

        val repository =
            GroceryRepositoryImpl(
                queries = queries,
                ioContext = UnconfinedTestDispatcher(),
            )

        suspendTest("select all returns things") {
        }

        isolationMode = IsolationMode.InstancePerTest
    })
