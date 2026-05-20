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
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        {
            val configuration =
                PHPickerConfiguration().apply {
                    selectionLimit = 1
                    filter = PHPickerFilter.imagesFilter()
                }
            val picker = PHPickerViewController(configuration = configuration)
            val delegate = ImagePickerDelegate { picked -> currentOnResult.value(picked) }
            picker.delegate = delegate
            retainedDelegates[picker] = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private val retainedDelegates = mutableMapOf<PHPickerViewController, ImagePickerDelegate>()

private class ImagePickerDelegate(private val onResult: (PickedImage?) -> Unit) :
    NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        retainedDelegates.remove(picker)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            deliver(null)
            return
        }
        val provider = result.itemProvider
        val typeIdentifier =
            when {
                provider.hasItemConformingToTypeIdentifier("public.jpeg") -> "public.jpeg"
                provider.hasItemConformingToTypeIdentifier("public.png") -> "public.png"
                else -> "public.image"
            }
        provider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, _ ->
            val bytes = data?.toByteArray()
            if (bytes == null) {
                deliver(null)
            } else {
                val extension =
                    when (typeIdentifier) {
                        "public.png" -> "png"
                        else -> "jpg"
                    }
                deliver(PickedImage(bytes = bytes, fileExtension = extension))
            }
        }
    }

    private fun deliver(result: PickedImage?) {
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
