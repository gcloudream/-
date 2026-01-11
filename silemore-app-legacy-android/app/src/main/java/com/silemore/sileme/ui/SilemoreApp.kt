package com.silemore.sileme.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.silemore.sileme.ui.screens.ContactsAddScreen
import com.silemore.sileme.ui.screens.ContactsScreen
import com.silemore.sileme.ui.screens.HistoryScreen
import com.silemore.sileme.ui.screens.HomeScreen
import com.silemore.sileme.ui.screens.LoginScreen
import com.silemore.sileme.ui.screens.RegisterScreen
import com.silemore.sileme.ui.screens.SettingsScreen
import com.silemore.sileme.ui.screens.SplashScreen
import com.silemore.sileme.viewmodel.AppViewModelFactory
import com.silemore.sileme.viewmodel.SessionViewModel

val LocalViewModelFactory = staticCompositionLocalOf<AppViewModelFactory> {
    error("AppViewModelFactory not provided")
}

@Composable
fun SilemoreApp() {
    val navController = rememberNavController()
    val factory = LocalViewModelFactory.current
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                sessionViewModel = sessionViewModel,
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                onOpenHistory = { navController.navigate("history") },
                onOpenContacts = { navController.navigate("contacts") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable("contacts") {
            ContactsScreen(
                onAdd = { navController.navigate("contacts_add") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("contacts_add") {
            ContactsAddScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(
                sessionViewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
