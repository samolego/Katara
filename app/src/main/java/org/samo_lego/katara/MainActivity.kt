package org.samo_lego.katara

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.samo_lego.katara.ui.KataraApp
import org.samo_lego.katara.ui.theme.KataraTheme
import org.samo_lego.katara.ui.viewmodel.TunerViewModel
import org.samo_lego.katara.util.Logger

class MainActivity : ComponentActivity() {
    // Create view models
    private val tunerViewModel: TunerViewModel by viewModels()

    // Permission launcher for requesting microphone access
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, start the tuner
            Logger.d("Audio recording permission granted")
            tunerViewModel.startTuner()
        } else {
            // Permission denied, inform the user
            Logger.d("Audio recording permission denied")
            // You could show a dialog here explaining why the permission is needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkAudioPermission()

        setContent {
            KataraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KataraApp(
                        tunerViewModel = tunerViewModel,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permission again on resume and start tuner if permission is granted
        if (hasAudioPermission()) {
            tunerViewModel.startTuner()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop the tuner when the app is paused
        tunerViewModel.stopTuner()
    }

    /**
        * Check if we have audio recording permission and request it if not
        */
    private fun checkAudioPermission() {
        when {
            // Permission already granted
            hasAudioPermission() -> {
                Logger.d("Audio recording permission already granted")
                tunerViewModel.startTuner()
            }
            // Show rationale - explain why we need this permission
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Logger.d("Should show permission rationale")
                // You could show a dialog here explaining why the permission is needed
                // and then request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            // Request permission directly
            else -> {
                Logger.d("Requesting audio recording permission")
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    /**
    * Check if we have audio recording permission
    */
    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
