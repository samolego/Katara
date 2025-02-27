package org.samo_lego.katara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.samo_lego.katara.ui.KataraApp
import org.samo_lego.katara.ui.theme.KataraTheme
import org.samo_lego.katara.viewmodel.GuitarTunerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KataraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KataraApp()
                }
            }
        }
    }
}
