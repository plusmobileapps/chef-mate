@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.featureflag.impl

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class Fnv1aTest {

    @Test
    fun fnv1a_known_vectors() {
        // Reference values for FNV-1a 32-bit. Verified against the standard reference impl.
        fnv1a("") shouldBe 2166136261u
        fnv1a("a") shouldBe 3826002220u
        fnv1a("foobar") shouldBe 3214735720u
    }

    @Test
    fun bucket_is_deterministic_and_within_range() {
        repeat(100) { i ->
            val b = bucket("flag", "user-$i")
            (b in 0..99) shouldBe true
        }
        val first = bucket("flag", "user-1")
        bucket("flag", "user-1") shouldBe first
    }

    @Test
    fun bucket_distribution_is_roughly_uniform() {
        val counts = IntArray(10)
        val sample = 10_000
        repeat(sample) { i ->
            val b = bucket("test_flag", "u-$i")
            counts[b / 10] += 1
        }
        // Each bucket of 10% should have roughly sample/10 = 1000 hits. Allow 30% slack to keep
        // the test stable across CI noise.
        val expected = sample / 10
        counts.forEach { (it in (expected * 7 / 10)..(expected * 13 / 10)) shouldBe true }
    }
}
