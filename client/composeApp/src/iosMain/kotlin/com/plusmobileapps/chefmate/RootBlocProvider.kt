package com.plusmobileapps.chefmate

import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.di.IosApplicationComponent
import com.plusmobileapps.chefmate.root.DeepLink
import com.plusmobileapps.chefmate.root.RootBloc
import com.plusmobileapps.chefmate.toast.ToastService
import dev.zacsweers.metro.createGraphFactory
import platform.UIKit.UIApplication

object RootBlocProvider {
    /**
     * The app-scoped [ToastService] from the same graph that built the [RootBloc]. Set by
     * [buildRootBloc] (always called before the UI is created) and read by [MainViewController] so
     * the toast host observes the very instance BLoCs enqueue onto. Threaded this way to avoid
     * widening the Swift glue signatures.
     */
    lateinit var toastService: ToastService
        private set

    /**
     * Bridge for handing the Supabase session to the watch companion over WatchConnectivity. Set by
     * [buildRootBloc] (always called before the UI is created) and read by the Swift
     * `WatchSessionSender`.
     */
    lateinit var sessionRelay: WatchSessionRelay
        private set

    fun buildRootBloc(
        componentContext: ComponentContext,
        application: UIApplication,
        deepLinkUrl: String? = null,
    ): RootBloc {
        val applicationComponent =
            createGraphFactory<IosApplicationComponent.Factory>().create(application)
        toastService = applicationComponent.toastService
        sessionRelay = applicationComponent.watchSessionRelay
        return applicationComponent.rootBlocFactory.create(
            context = DefaultBlocContext(componentContext = componentContext),
            deepLink = DeepLink.parse(deepLinkUrl),
        )
    }
}
