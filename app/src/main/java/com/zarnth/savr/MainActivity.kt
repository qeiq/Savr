package com.zarnth.savr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.zarnth.savr.presentation.root.RootScreen
import com.zarnth.savr.ui.theme.SavrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            else -> null
        }

        setContent {
            SavrTheme {
                RootScreen(sharedUrl = sharedUrl)
            }
        }
    }
}

fun openChromeTab(url: String, context: Context) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(context, url.toUri())
}
