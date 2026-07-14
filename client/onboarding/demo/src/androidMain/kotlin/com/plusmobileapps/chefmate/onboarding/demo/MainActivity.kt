package com.plusmobileapps.chefmate.onboarding.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext

class MainActivity : ComponentActivity() {
    private lateinit var bloc: OnboardingDemoBloc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val component = (application as OnboardingDemoApplication).component
        bloc =
            buildOnboardingDemoBloc(
                componentContext = defaultComponentContext(),
                component = component,
            )
        setContent { OnboardingDemoApp(bloc) }
    }
}
