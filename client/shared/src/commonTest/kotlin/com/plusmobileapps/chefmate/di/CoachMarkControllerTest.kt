@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.di

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val A = "a"
private const val B = "b"

class CoachMarkControllerTest {

    private fun controller() = CoachMarkController(MapSettings())

    @Test
    fun When_nothing_requested_Then_no_active_coach_mark() {
        assertNull(controller().activeCoachMark.value)
    }

    @Test
    fun When_requested_Then_it_becomes_active() {
        val controller = controller()
        controller.request(A)
        assertEquals(A, controller.activeCoachMark.value)
    }

    @Test
    fun When_two_requested_Then_only_the_first_is_active() {
        val controller = controller()
        controller.request(A)
        controller.request(B)
        assertEquals(A, controller.activeCoachMark.value)
    }

    @Test
    fun When_active_dismissed_Then_next_becomes_active_and_first_is_seen() {
        val controller = controller()
        controller.request(A)
        controller.request(B)

        controller.dismiss(A)

        assertEquals(B, controller.activeCoachMark.value)
        assertTrue(controller.hasSeen(A))
        assertFalse(controller.hasSeen(B))
    }

    @Test
    fun When_already_seen_Then_request_is_ignored() {
        val controller = controller()
        controller.dismiss(A)
        controller.request(A)
        assertNull(controller.activeCoachMark.value)
    }

    @Test
    fun When_released_Then_it_is_not_seen_and_can_be_requested_again() {
        val controller = controller()
        controller.request(A)
        controller.release(A)

        assertNull(controller.activeCoachMark.value)
        assertFalse(controller.hasSeen(A))

        controller.request(A)
        assertEquals(A, controller.activeCoachMark.value)
    }

    @Test
    fun When_released_Then_the_next_mark_becomes_active() {
        val controller = controller()
        controller.request(A)
        controller.request(B)

        controller.release(A)

        assertEquals(B, controller.activeCoachMark.value)
        assertFalse(controller.hasSeen(A))
    }

    @Test
    fun When_requested_twice_Then_it_is_not_duplicated_in_the_queue() {
        val controller = controller()
        controller.request(A)
        controller.request(A)
        controller.request(B)

        // If A were duplicated, dismissing once would leave A still active.
        controller.dismiss(A)
        assertEquals(B, controller.activeCoachMark.value)
    }

    @Test
    fun When_clearAllSeen_Then_previously_seen_marks_can_show_again() {
        val controller = controller()
        controller.dismiss(A)
        controller.dismiss(B)
        assertTrue(controller.hasSeen(A))

        controller.clearAllSeen()

        assertFalse(controller.hasSeen(A))
        assertFalse(controller.hasSeen(B))
        controller.request(A)
        assertEquals(A, controller.activeCoachMark.value)
    }

    @Test
    fun When_clearAllSeen_Then_unrelated_settings_are_untouched() {
        val settings = MapSettings()
        settings.putString("unrelated_key", "keep")
        val controller = CoachMarkController(settings)
        controller.dismiss(A)

        controller.clearAllSeen()

        assertFalse(controller.hasSeen(A))
        assertTrue(settings.hasKey("unrelated_key"))
    }

    @Test
    fun When_seen_persisted_in_settings_Then_hasSeen_reads_it_back() {
        val settings = MapSettings()
        CoachMarkController(settings).dismiss(A)

        // A fresh controller over the same settings still reports the mark as seen.
        assertTrue(CoachMarkController(settings).hasSeen(A))
    }
}
