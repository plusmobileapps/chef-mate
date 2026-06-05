package com.plusmobileapps.chefmate.recipe.data.impl

import com.plusmobileapps.chefmate.recipe.data.PendingPhoto
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PendingRecipePhotoStoreImplTest {

    private val store = PendingRecipePhotoStoreImpl()

    @Test
    fun put_then_consume_returns_the_photo() {
        store.put(byteArrayOf(1, 2, 3), "jpg")

        store.consume() shouldBe PendingPhoto(byteArrayOf(1, 2, 3), "jpg")
    }

    @Test
    fun consume_clears_the_slot() {
        store.put(byteArrayOf(1, 2, 3), "jpg")
        store.consume()

        store.consume().shouldBeNull()
    }

    @Test
    fun consume_without_put_returns_null() {
        store.consume().shouldBeNull()
    }

    @Test
    fun put_overwrites_previous_photo() {
        store.put(byteArrayOf(1), "jpg")
        store.put(byteArrayOf(2), "png")

        store.consume() shouldBe PendingPhoto(byteArrayOf(2), "png")
    }
}
