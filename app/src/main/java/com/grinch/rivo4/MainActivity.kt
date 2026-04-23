package com.grinch.rivo4

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.grinch.rivo4.view.theme.Rivo4Theme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberNavHostEngine
// ✅ تصحيح مسارات الاستيراد للملفات المولدة تلقائياً
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ContactDetailsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ContactEditScreenDestination
import com.ramcosta.composedestinations.generated.destinations.DialPadScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MorphingOnboardingScreenDestination
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {

    private val requestRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
            }
        }

        requestDialerRole()

        setContent {
            Rivo4Theme {
                val navController = rememberNavController()
                val context = androidx.compose.ui.platform.LocalContext.current

                val sharedPref = remember { context.getSharedPreferences("pdialer_prefs", Context.MODE_PRIVATE) }
                val isFirstLaunch = remember { sharedPref.getBoolean("is_first_launch", true) }

                val navHostEngine = rememberNavHostEngine(
                    rootDefaultAnimations = RootNavGraphDefaultAnimations(
                        enterTransition = {
                            scaleIn(
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                initialScale = 0.9f
                            ) + fadeIn()
                        },
                        exitTransition = { fadeOut() }
                    )
                )

                // ✅ تصحيح: استخدام startRoute بدلاً من startDestination بناءً على رسالة الخطأ
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                                        // ✅ استخدام startRoute ليتوافق مع نسخة المكتبة 2.1.0
                                        startDestination = if (isFirstLaunch) MorphingOnboardingScreenDestination else DialPadScreenDestination,
                                        navController = navController,
                                        engine = navHostEngine
                                    )

                LaunchedEffect(intent) {
                    handleIntent(intent, navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?, navController: androidx.navigation.NavController) {
        intent ?: return
        val data = intent.data
        val action = intent.action

        when (action) {
            Intent.ACTION_DIAL, Intent.ACTION_VIEW -> {
                if (data?.scheme == "tel") {
                    val number = data.schemeSpecificPart
                    navController.navigate(DialPadScreenDestination(initialNumber = number).route)
                } else if (data?.toString()?.contains("contacts") == true ||
                           data?.toString()?.contains("com.android.contacts") == true ||
                           intent.hasExtra("contact_id")) {
                    val id = data?.lastPathSegment ?: intent.getStringExtra("contact_id")
                    if (id != null) {
                        navController.navigate(ContactDetailsScreenDestination(contactId = id).route)
                    }
                }
            }
            Intent.ACTION_INSERT -> {
                val name = intent.getStringExtra(ContactsContract.Intents.Insert.NAME)
                val phone = intent.getStringExtra(ContactsContract.Intents.Insert.PHONE)
                navController.navigate(ContactEditScreenDestination(initialName = name, initialPhone = phone).route)
            }
            Intent.ACTION_EDIT -> {
                val id = data?.lastPathSegment
                if (id != null) {
                    navController.navigate(ContactEditScreenDestination(contactId = id).route)
                }
            }
        }
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                requestRoleLauncher.launch(intent)
            }
        }
    }
}
