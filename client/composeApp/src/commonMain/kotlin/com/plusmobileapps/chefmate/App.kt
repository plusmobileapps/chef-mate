package com.plusmobileapps.chefmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.root.RootScreen

@Composable
fun App(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }
    RootScreen(rootBloc, modifier)
}
