package fr.rudy.karoostamina.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.rudy.karoostamina.StaminaPrefs
import fr.rudy.karoostamina.StaminaSettings

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val current = remember { StaminaPrefs.load(ctx) }
    var cp by remember { mutableStateOf(current.cpWatts.toString()) }
    var wp by remember { mutableStateOf(trimFloat(current.wPrimeKj)) }
    var cap by remember { mutableStateOf(current.capacityKj.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Réglages Stamina", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Valeurs lues au démarrage d'une sortie. Un changement en cours de sortie ne s'applique qu'à la suivante.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OutlinedTextField(
            value = cp,
            onValueChange = { cp = it; saved = false; error = null },
            label = { Text("CP (W), ${StaminaPrefs.CP_MIN} à ${StaminaPrefs.CP_MAX}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = wp,
            onValueChange = { wp = it; saved = false; error = null },
            label = { Text("W' (kJ), ${trimFloat(StaminaPrefs.WPRIME_MIN)} à ${trimFloat(StaminaPrefs.WPRIME_MAX)}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = cap,
            onValueChange = { cap = it; saved = false; error = null },
            label = { Text("Capacité d'endurance (kJ), 0 = auto") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (saved) Text("Réglages enregistrés", color = MaterialTheme.colorScheme.onBackground)
        Button(
            onClick = {
                val cpV = cp.trim().toIntOrNull()
                val wpV = wp.trim().replace(',', '.').toFloatOrNull()
                val capV = cap.trim().toIntOrNull()
                error = when {
                    cpV == null || cpV !in StaminaPrefs.CP_MIN..StaminaPrefs.CP_MAX ->
                        "CP : entre ${StaminaPrefs.CP_MIN} et ${StaminaPrefs.CP_MAX} W"
                    wpV == null || wpV < StaminaPrefs.WPRIME_MIN || wpV > StaminaPrefs.WPRIME_MAX ->
                        "W' : entre ${trimFloat(StaminaPrefs.WPRIME_MIN)} et ${trimFloat(StaminaPrefs.WPRIME_MAX)} kJ"
                    capV == null || (capV != 0 && capV !in StaminaPrefs.CAPACITY_MIN..StaminaPrefs.CAPACITY_MAX) ->
                        "Capacité : 0 (auto) ou entre ${StaminaPrefs.CAPACITY_MIN} et ${StaminaPrefs.CAPACITY_MAX} kJ"
                    else -> null
                }
                if (error == null) {
                    StaminaPrefs.save(ctx, StaminaSettings(cpV!!, wpV!!, capV!!))
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Enregistrer") }
    }
}

private fun trimFloat(v: Float): String = if (v % 1f == 0f) v.toInt().toString() else v.toString()
