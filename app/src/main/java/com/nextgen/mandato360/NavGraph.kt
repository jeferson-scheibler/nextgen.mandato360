package com.nextgen.mandato360

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nextgen.mandato360.ui.DashboardScreen
import com.nextgen.mandato360.ui.RoleScreen
import com.nextgen.mandato360.ui.UploadPhotoScreen

@Composable
fun MandatoNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login")  { LoginScreen(navController) }
        composable("role")   { RoleScreen(navController) }
        composable("uploadPhoto") { UploadPhotoScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
    }
}