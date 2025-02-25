package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.R
import org.samo_lego.katara.ui.components.TunerLayout
import org.samo_lego.katara.util.GuitarString
import org.samo_lego.katara.util.TuningDirection

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KataraApp() {
    var activeString = remember { mutableStateOf<GuitarString?>(null) }
    var tuningDirection = remember { mutableStateOf(TuningDirection.NONE) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    TunerLayout(
                            activeString = activeString.value,
                            tuningDirection = tuningDirection.value,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        GuitarComponent(
                            activeString = activeString.value,
                            tuningDirection = tuningDirection,
                            onActiveStringChange = { guitarString ->
                                activeString.value = if (activeString.value == guitarString) null else guitarString
                            }
                        )
                }
            }
        }
    }
}
