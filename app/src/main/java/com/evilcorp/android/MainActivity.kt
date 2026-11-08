package com.evilcorp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.ui.EvilCorpApp
import com.evilcorp.android.ui.EvilCorpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = ProgressStore(applicationContext)
        setContent { EvilCorpTheme { EvilCorpApp(store) } }
    }
}
