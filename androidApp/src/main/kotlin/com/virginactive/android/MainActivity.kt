package com.virginactive.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.virginactive.android.nav.VaNavHost
import com.virginactive.android.theme.VaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaTheme {
                VaNavHost()
            }
        }
    }
}
