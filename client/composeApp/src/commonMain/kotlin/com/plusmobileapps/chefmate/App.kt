package com.plusmobileapps.chefmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootScreen
import com.plusmobileapps.chefmate.toast.ToastScaffold
import com.plusmobileapps.chefmate.toast.ToastService

@Composable
fun App(rootBloc: RootBloc, toastService: ToastService, modifier: Modifier = Modifier) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                // The gif decoder is platform-specific: coil3.gif.GifDecoder/AnimatedImageDecoder
                // only exist on Android, while other targets use the Skia-based decoder.
                addAnimatedGifDecoder()
            }
            .build()
    }
    ToastScaffold(toastService = toastService) { RootScreen(rootBloc, modifier) }
}
