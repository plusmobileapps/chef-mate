package com.plusmobileapps.chefmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appComponent = (application as MyApplication).appComponent
        val rootBloc = buildRootBloc(
            componentContext = defaultComponentContext(),
            applicationComponent = appComponent,
        )
        setContent {
            App(rootBloc)
        }
    }
}