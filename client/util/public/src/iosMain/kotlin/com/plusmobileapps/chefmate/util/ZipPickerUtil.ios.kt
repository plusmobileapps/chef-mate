@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalForeignApi::class)

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeZIP
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberZipPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeZIP))
            val delegate = ZipPickerDelegate { picked -> currentOnResult.value(picked) }
            picker.delegate = delegate
            retainedDelegates[picker] = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private val retainedDelegates = mutableMapOf<UIDocumentPickerViewController, ZipPickerDelegate>()

private class ZipPickerDelegate(private val onResult: (PickedFile?) -> Unit) :
    NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        retainedDelegates.remove(controller)
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            deliver(null)
            return
        }
        val accessed = url.startAccessingSecurityScopedResource()
        val data = NSData.dataWithContentsOfURL(url)
        if (accessed) url.stopAccessingSecurityScopedResource()
        val bytes = data?.toByteArray()
        if (bytes == null) {
            deliver(null)
            return
        }
        deliver(PickedFile(bytes = bytes, fileName = url.lastPathComponent ?: "import.zip"))
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        retainedDelegates.remove(controller)
        deliver(null)
    }

    private fun deliver(result: PickedFile?) {
        dispatch_async(dispatch_get_main_queue()) { onResult(result) }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).also { array ->
        array.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
    }
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
