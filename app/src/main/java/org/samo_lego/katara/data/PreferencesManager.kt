package org.samo_lego.katara.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.samo_lego.katara.data.proto.AppSettings
import org.samo_lego.katara.instrument.InstrumentLayoutSpecification

// Create a DataStore instance at the top level using Proto DataStore
private val Context.appSettingsDataStore: DataStore<AppSettings> by dataStore(
    fileName = "app_settings.pb",
    serializer = AppSettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler {
        AppSettingsSerializer.defaultValue
    }
)

class PreferencesManager(private val context: Context) {

    // Get the current text visibility state as a Flow
    val dedicationTextVisibility: Flow<Boolean> = context.appSettingsDataStore.data
        .map { appSettings ->
            appSettings.showDedicationText
        }

    // Get the selected instrument as a Flow
    val selectedInstrument: Flow<InstrumentLayoutSpecification> = context.appSettingsDataStore.data
        .map { appSettings ->
            // Get the stored instrument name, or default to GUITAR_STANDARD
            val instrumentName = appSettings.selectedInstrument
            // Find the instrument with this name, or default to GUITAR_STANDARD
            InstrumentLayoutSpecification.availableInstruments.find { it.name == instrumentName }
                ?: InstrumentLayoutSpecification.GUITAR_STANDARD
        }

    // Update the text visibility state
    suspend fun updateDedicationTextVisibility(isVisible: Boolean) {
        context.appSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setShowDedicationText(isVisible)
                .build()
        }
    }

    // Update the selected instrument
    suspend fun updateSelectedInstrument(instrument: InstrumentLayoutSpecification) {
        context.appSettingsDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setSelectedInstrument(instrument.name)
                .build()
        }
    }
}
