package com.plusmobileapps.chefmate.subscription

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.subscription.data.SubscriptionPackage
import com.plusmobileapps.chefmate.subscription.ui.SubscriptionScreen
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

/** Premium paywall screen. Presented as a root child from the AI-chat gate and the More tab. */
interface SubscriptionBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        SubscriptionScreen(bloc = this, modifier = modifier)
    }

    fun onCloseClicked()

    fun onPackageSelected(packageId: String)

    fun onPurchaseClicked()

    fun onRestoreClicked()

    fun onErrorDismissed()

    data class Model(
        /**
         * True once the premium entitlement is active — the screen becomes a confirmation state.
         */
        val isPremium: Boolean = false,
        /** True while the first offering load is in flight. */
        val isLoading: Boolean = true,
        val packages: List<SubscriptionPackage> = emptyList(),
        val selectedPackageId: String? = null,
        /** True while a purchase or restore is in flight; disables the CTA and shows a spinner. */
        val isProcessing: Boolean = false,
        /** Non-null when the last purchase/restore failed; drives the error dialog. */
        val error: TextData? = null,
    )

    sealed class Output {
        /** Close the paywall (user dismissed, or a purchase completed). */
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SubscriptionBloc
    }
}
