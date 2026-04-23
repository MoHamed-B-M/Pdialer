// File: app/src/main/java/com/grinch/rivo4/view/screens/LauncherScreen.kt
package com.grinch.rivo4.view.screens  // ✅ Match your other screens' package

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.DialPadScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MorphingOnboardingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import android.content.Context

@Destination<RootGraph>(start = true)  // ✅ This makes it the start destination
@Composable
fun LauncherScreen(
    navigator: DestinationsNavigator,  // ✅ Use DestinationsNavigator, not NavController
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("pdialer_prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        val isFirstLaunch = sharedPref.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            navigator.navigate(MorphingOnboardingScreenDestination) {
                popUpTo(LauncherScreenDestination) {
                    inclusive = true
                }
            }
            sharedPref.edit().putBoolean("is_first_launch", false).apply()
        } else {
            navigator.navigate(DialPadScreenDestination) {
                popUpTo(LauncherScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    // Loading UI while deciding where to go
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
