package com.silemore.sileme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.silemore.sileme.ui.SilemoreApp
import com.silemore.sileme.ui.LocalViewModelFactory
import com.silemore.sileme.ui.theme.SilemoreTheme
import com.silemore.sileme.util.AppContainer
import com.silemore.sileme.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = remember { AppContainer(applicationContext) }
            val factory = remember { AppViewModelFactory(container.repository, container.tokenStore) }

            CompositionLocalProvider(LocalViewModelFactory provides factory) {
                SilemoreTheme {
                    SilemoreApp()
                }
            }
        }
    }
}
