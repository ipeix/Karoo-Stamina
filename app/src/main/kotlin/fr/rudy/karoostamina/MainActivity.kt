package fr.rudy.karoostamina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.rudy.karoostamina.screens.SettingsScreen
import fr.rudy.karoostamina.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                SettingsScreen()
            }
        }
    }
}
