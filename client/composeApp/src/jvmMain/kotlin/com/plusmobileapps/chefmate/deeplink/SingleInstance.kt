package com.plusmobileapps.chefmate.deeplink

import co.touchlab.kermit.Logger
import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Keeps the desktop app to a single window and routes deep links from later launches into the
 * running instance.
 *
 * On Windows and Linux the OS starts a **new process** every time `chefmate://…` is opened, so
 * without this a second window would appear and the already-open app would never see the link. The
 * first process wins a file lock and becomes the primary, listening on a loopback socket; any later
 * process forwards its link there and exits. macOS is exempt — LaunchServices reuses the single
 * running instance and delivers subsequent opens as Apple Events (handled in main.kt), so no socket
 * is involved.
 *
 * Best-effort: if the lock/socket machinery fails for any reason (e.g. a sandbox forbids the
 * listening socket), the process proceeds as primary rather than refusing to start.
 */
object SingleInstance {
    private val log = Logger.withTag("SingleInstance")

    /**
     * Returns true if this process should build the UI (it is the primary instance). Returns false
     * only when another instance already owns the app — in that case the deep link from [args] has
     * been forwarded to it and this process must exit without opening a window.
     */
    fun acquireOrForward(args: Array<String>): Boolean {
        // macOS enforces single-instance itself and delivers links via Apple Event; no socket
        // needed.
        if (desktopOs() == DesktopOs.MACOS) return true
        return runCatching { acquire(args) }
            .getOrElse {
                log.w(it) { "single-instance setup failed; proceeding as primary" }
                true
            }
    }

    private fun acquire(args: Array<String>): Boolean {
        val dir =
            File(System.getProperty("java.io.tmpdir"), "chefmate-singleinstance").apply { mkdirs() }
        val portFile = File(dir, "port")
        // The channel/lock are intentionally never closed: the lock is held for the whole process
        // lifetime and released by the OS on exit, which is exactly the primary-election signal.
        val lock = RandomAccessFile(File(dir, "lock"), "rw").channel.tryLock()

        if (lock == null) {
            // Another live instance holds the lock. Hand off our link and step aside.
            forward(portFile, args.firstOrNull())
            return false
        }

        startServer(portFile)
        return true
    }

    private fun startServer(portFile: File) {
        val server = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
        portFile.writeText(server.localPort.toString())
        Thread(
                {
                    while (!server.isClosed) {
                        runCatching {
                            server.accept().use { socket ->
                                val link = socket.getInputStream().bufferedReader().readLine()
                                DeepLinkCoordinator.submit(link)
                            }
                        }
                            .onFailure { log.w(it) { "single-instance accept failed" } }
                    }
                },
                "chefmate-singleinstance",
            )
            .apply { isDaemon = true }
            .start()
    }

    private fun forward(portFile: File, link: String?) {
        if (link.isNullOrBlank()) return
        runCatching {
            val port = portFile.readText().trim().toInt()
            Socket(InetAddress.getLoopbackAddress(), port).use { socket ->
                socket.getOutputStream().bufferedWriter().apply {
                    write(link)
                    newLine()
                    flush()
                }
            }
        }
            .onFailure { log.w(it) { "failed to forward deep link to the primary instance" } }
    }
}
