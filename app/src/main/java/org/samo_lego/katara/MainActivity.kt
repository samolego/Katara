package org.samo_lego.katara

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.samo_lego.katara.ui.KataraApp
import org.samo_lego.katara.ui.theme.KataraTheme
import org.samo_lego.katara.ui.viewmodel.TunerViewModel

class MainActivity : ComponentActivity() {
    // Create view model
    private val tunerViewModel: TunerViewModel by viewModels()
    private val loggerTag = "MainActivity"

    // Permission launcher for requesting microphone access
    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted: Boolean ->
                if (isGranted) {
                    // Permission granted, start the tuner
                    Log.d(loggerTag, "Audio recording permission granted")
                    tunerViewModel.startTuner()
                } else {
                    // Permission denied, inform the user
                    Log.d(loggerTag, "Audio recording permission denied")
                    // You could show a dialog here explaining why the permission is needed
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkAudioPermission()

        setContent {
            KataraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KataraApp(tunerViewModel = tunerViewModel)
                }
            }
        }
    }

    private fun setupSplashScreen() {
        var keepSplashOnScreen = true
        installSplashScreen().apply { setKeepOnScreenCondition { keepSplashOnScreen } }

        // Set the theme after the splash screen is installed
        setTheme(R.style.Theme_Katara)

        // Launch a coroutine to dismiss splash screen after delay
        lifecycleScope.launch {
            delay(1100) // Wait for animation + buffer
            keepSplashOnScreen = false
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

    /** Check if we have audio recording permission and request it if not */
    private fun checkAudioPermission() {
        when {
            // Permission already granted
            hasAudioPermission() -> {
                Log.d(loggerTag, "Audio recording permission already granted")
                tunerViewModel.startTuner()
            }
            // Show rationale - explain why we need this permission
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Log.d(loggerTag, "Should show permission rationale")
                // You could show a dialog here explaining why the permission is needed
                // and then request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            // Request permission directly
            else -> {
                Log.d(loggerTag, "Requesting audio recording permission")
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    /** Check if we have audio recording permission */
    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }
}
