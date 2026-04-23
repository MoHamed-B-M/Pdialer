// File: app/src/main/java/com/grinch/rivo4/view/screen/LauncherScreen.kt
package com.grinch.rivo4.view.screen

import android.content.Context
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

@Destination<RootGraph>(start = true)
@Composable
fun LauncherScreen(
    navigator: DestinationsNavigator,
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("pdialer_prefs", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        val isFirstLaunch = sharedPref.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            // ✅ Simple navigation - NO builder lambda to avoid API mismatches
            navigator.navigate(MorphingOnboardingScreenDestination)
            sharedPref.edit().putBoolean("is_first_launch", false).apply()
        } else {
            // ✅ Navigate to main screen
            navigator.navigate(DialPadScreenDestination)
        }
    }

    // Loading UI while deciding destination
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
