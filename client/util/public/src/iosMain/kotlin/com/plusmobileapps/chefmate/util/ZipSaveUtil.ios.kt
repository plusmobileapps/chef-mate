@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        { fileName, bytes ->
            val tempPath = NSTemporaryDirectory() + fileName
            // UIDocumentPickerViewController(forExportingURLs:) needs an on-disk file URL, so the
            // bytes have to land in a temp file before the picker is presented. The temp file is
            // cleaned up after the picker dismisses.
            val data = bytes.toNSData()
            val written = data.writeToFile(tempPath, atomically = true)
            if (!written) {
                currentOnResult.value(false)
                return@remember
            }
            val url = NSURL.fileURLWithPath(tempPath)
            val picker =
                UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
            val delegate =
                ZipSaveDelegate(tempPath = tempPath, onResult = { ok -> currentOnResult.value(ok) })
            picker.delegate = delegate
            retainedDelegates[picker] = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private val retainedDelegates = mutableMapOf<UIDocumentPickerViewController, ZipSaveDelegate>()

private class ZipSaveDelegate(
    private val tempPath: String,
    private val onResult: (Boolean) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        cleanup(controller)
        deliver(didPickDocumentsAtURLs.isNotEmpty())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        cleanup(controller)
        deliver(false)
    }

    private fun cleanup(controller: UIDocumentPickerViewController) {
        retainedDelegates.remove(controller)
        NSFileManager.defaultManager.removeItemAtPath(tempPath, null)
    }

    private fun deliver(result: Boolean) {
        dispatch_async(dispatch_get_main_queue()) { onResult(result) }
    }
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

private fun topViewController(): UIViewController? {
    val keyWindow =
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.map { w -> w as UIWindow } }
            .firstOrNull { it.isKeyWindow() }
    var topVC: UIViewController? = keyWindow?.rootViewController
    while (topVC?.presentedViewController != null) {
        topVC = topVC.presentedViewController
    }
    return topVC
}
