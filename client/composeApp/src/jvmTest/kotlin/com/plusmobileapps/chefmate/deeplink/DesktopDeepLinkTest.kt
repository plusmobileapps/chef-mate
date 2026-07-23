package com.plusmobileapps.chefmate.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemeRegistrarTest {
    @Test
    fun windows_reg_file_maps_the_scheme_to_the_quoted_launcher() {
        val reg =
            SchemeRegistrar.windowsRegFileContent("C:\\Program Files\\Chef Mate\\Chef Mate.exe")

        assertTrue(reg.startsWith("Windows Registry Editor Version 5.00"), reg)
        assertTrue(reg.contains("[HKEY_CURRENT_USER\\Software\\Classes\\chefmate]"), reg)
        assertTrue(reg.contains("\"URL Protocol\"=\"\""), reg)
        // The command value doubles backslashes and escapes quotes; %1 is the opened URL.
        assertTrue(
            reg.contains(
                "@=\"\\\"C:\\\\Program Files\\\\Chef Mate\\\\Chef Mate.exe\\\" \\\"%1\\\"\""
            ),
            reg,
        )
    }

    @Test
    fun linux_desktop_file_declares_the_scheme_handler() {
        val desktop = SchemeRegistrar.linuxDesktopFileContent("/opt/chef-mate/bin/Chef Mate")

        assertTrue(desktop.contains("[Desktop Entry]"), desktop)
        assertTrue(desktop.contains("Exec=\"/opt/chef-mate/bin/Chef Mate\" %u"), desktop)
        assertTrue(desktop.contains("MimeType=x-scheme-handler/chefmate;"), desktop)
        assertTrue(desktop.contains("NoDisplay=true"), desktop)
    }
}

class DeepLinkCoordinatorTest {
    @Test
    fun submit_retains_the_trimmed_link_for_a_late_collector() {
        DeepLinkCoordinator.submit("  chefmate://notifications  ")

        assertEquals("chefmate://notifications", DeepLinkCoordinator.links.replayCache.last())
    }

    @Test
    fun submit_ignores_null_and_blank_links() {
        DeepLinkCoordinator.submit("chefmate://groceries")
        val before = DeepLinkCoordinator.links.replayCache.last()

        DeepLinkCoordinator.submit(null)
        DeepLinkCoordinator.submit("   ")

        assertEquals(before, DeepLinkCoordinator.links.replayCache.last())
    }
}
