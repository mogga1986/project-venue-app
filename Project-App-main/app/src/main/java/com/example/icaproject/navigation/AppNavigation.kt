package com.example.icaproject.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.icaproject.pages.LoginSignup.LoginScreen
import com.example.icaproject.pages.LoginSignup.RegisterACompanyScreen
import com.example.icaproject.pages.LoginSignup.RegistrationScreen
import com.example.icaproject.pages.MainScreens.AdminScreens.ModifyCompaniesScreen
import com.example.icaproject.pages.MainScreens.AdminScreens.ModifyUsersScreen
import com.example.icaproject.pages.MainScreens.AdminScreens.ViewPayments
import com.example.icaproject.pages.MainScreens.HomeScreen
import com.example.icaproject.pages.MainScreens.ListAVenueScreen
import com.example.icaproject.pages.MainScreens.VenueScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Login") {
        composable("Login") { LoginScreen(navController) { navController.navigate("HomeScreen") } }
        composable("Register") { RegistrationScreen(navController) { navController.navigate("Login") }}
        composable("HomeScreen") { HomeScreen(navController) }
        composable("VenueScreen") { VenueScreen(navController) }
        composable("ListAVenueScreen") { ListAVenueScreen(navController) }
        composable("RegisterACompanyScreen") { RegisterACompanyScreen(navController) { navController.navigate("Register") }}
        composable("ModifyUsersScreen") { ModifyUsersScreen(navController)}
        composable("ViewPayments") { ViewPayments(navController)}
        composable("ModifyCompaniesScreen") { ModifyCompaniesScreen(navController)}

    }
}