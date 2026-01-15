package com.plusmobileapps.chefmate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    private lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appComponent = (application as MyApplication).appComponent
        supabaseClient = appComponent.supabaseClient

        val rootBloc =
            buildRootBloc(
                componentContext = defaultComponentContext(),
                applicationComponent = appComponent,
            )
        setContent {
            App(rootBloc)
        }

        supabaseClient.handleDeeplinks(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}
