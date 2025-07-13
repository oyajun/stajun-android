package com.oyajun.stajun

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oyajun.stajun.ui.login.LoginEmailScreen
import com.oyajun.stajun.ui.login.LoginOtpScreen

@Composable
fun StaJunApp() {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        NavigationHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login_email",
        modifier = modifier.fillMaxSize()
    ) {
        composable("login_email") {
            val loginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.oyajun.stajun.ui.login.LoginViewModel>()
            val loginData = loginViewModel.loginData.collectAsState().value
            LoginEmailScreen(
                email = loginData.email,
                onEmailChange = { loginViewModel.updateEmail(it) },
                onNext = { loginViewModel.submitEmail(); navController.navigate("login_otp") },
                enabled = loginData.email.isNotBlank()
            )
        }
        composable("login_otp") {
            val loginViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.oyajun.stajun.ui.login.LoginViewModel>()
            val loginData = loginViewModel.loginData.collectAsState().value
            LoginOtpScreen(
                email = loginData.email,
                otp = loginData.otp,
                onOtpChange = { loginViewModel.updateOtp(it) },
                onLogin = { loginViewModel.submitOtp() },
                loginState = loginData.loginState,
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen()
        }
        composable("details") {
            DetailsScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    Text("Home Screen", Modifier.fillMaxSize())
}

@Composable
fun DetailsScreen() {
    Text("Details Screen", Modifier.fillMaxSize())
}
