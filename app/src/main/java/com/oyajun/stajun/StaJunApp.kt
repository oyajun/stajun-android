package com.oyajun.stajun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oyajun.stajun.model.LoginState
import com.oyajun.stajun.ui.login.LoginEmailScreen
import com.oyajun.stajun.ui.login.LoginOtpScreen
import com.oyajun.stajun.ui.login.LoginViewModel

// ナビゲーションルートの定義
sealed class Screen(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit,
    val showNavigationBar: Boolean = true,
    val isMainScreen: Boolean = false // メイン画面かどうかのフラグを追加
) {
    object Home : Screen("home", "ホーム", { Icon(Icons.Filled.Home, contentDescription = "ホーム") }, isMainScreen = true)
    object Record : Screen("record", "記録", { Icon(Icons.Filled.Create, contentDescription = "記録") }, isMainScreen = true)
    object Profile : Screen("profile", "プロフィール", { Icon(Icons.Filled.Person, contentDescription = "プロフィール") }, isMainScreen = true)
    object Detail : Screen("detail", "詳細", { /* 詳細画面にはアイコンは不要 */ }, showNavigationBar = true)
    object LoginEmail : Screen("login-email", "ログイン", { /* ログイン画面にはアイコンは不要 */ }, showNavigationBar = false)
    object LoginOtp : Screen("login-otp", "OTPログイン", { /* OTPログイン画面にはアイコンは不要 */ }, showNavigationBar = false)
}

@Composable
fun StaJunApp(initialLoginState: Boolean = false) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    // 仮のログイン状態管理（MainActivityから初期値を受け取る）
    var isLoggedIn by remember { mutableStateOf(initialLoginState) }
    var isInitialized by remember { mutableStateOf(true) } // MainActivityで既に初期化済み

    // NavigationBarで表示するアイテムのリスト（メイン画面のみ）
    val navBarItems = listOf(Screen.Home, Screen.Record, Screen.Profile)

    // 全ての画面を含むリスト
    val allScreens = listOf(Screen.Home, Screen.Record, Screen.Profile, Screen.Detail, Screen.LoginEmail, Screen.LoginOtp)

    // 現在のルートとバックスタックエントリを取得
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // NavigationBarの表示/非表示を判断
    val shouldShowNavigationBar = when {
        !isLoggedIn -> false // ログインしていない場合は表示しない
        currentRoute?.contains("/") == true -> {
            // パラメータ付きルート（例: "detail/home"）の場合
            val baseRoute = currentRoute.split("/")[0]
            allScreens.find { it.route == baseRoute }?.showNavigationBar ?: false
        }
        else -> {
            // 通常のルートの場合
            allScreens.find { it.route == currentRoute }?.showNavigationBar ?: false
        }
    }

    // 選択状態を決定（詳細画面では遷移元を選択状態にする）
    val selectedRoute = when {
        currentRoute?.startsWith("detail/") == true -> {
            // 詳細画面の場合、URLパラメータから遷移元を取得
            currentRoute.substringAfter("detail/")
        }
        else -> currentRoute
    }

    // ログイン状態の監視
    LaunchedEffect(loginViewModel.loginData.collectAsState().value.loginState) {
        val loginState = loginViewModel.loginData.value.loginState
        when (loginState) {
            LoginState.EMAIL_SUCCESS -> {
                navController.navigate(Screen.LoginOtp.route) {
                    popUpTo(Screen.LoginEmail.route) { inclusive = true }
                }
            }
            LoginState.OTP_SUCCESS -> {
                // OTP認証成功時はログイン状態を更新してホーム画面へ遷移
                isLoggedIn = true
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true } // 全てのバックスタックをクリア
                }
            }
            else -> {}
        }
    }


    Scaffold(
        bottomBar = {
            if (shouldShowNavigationBar) {
                NavigationBar {
                    navBarItems.forEach { item ->
                        NavigationBarItem(
                            icon = { item.icon() },
                            label = { Text(item.title) },
                            selected = selectedRoute == item.route,
                            onClick = {
                                if (selectedRoute == item.route && currentRoute?.startsWith("detail/") == true) {
                                    // 詳細画面で現在選択されている画面のボタンを押した場合、その画面に遷移
                                    navController.navigate(item.route) {
                                        popUpTo(item.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else if (currentRoute != item.route) {
                                    // 通常の画面遷移
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.LoginEmail.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Record.route) {
                SearchScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable("detail/{from}") { backStackEntry ->
                val from = backStackEntry.arguments?.getString("from") ?: Screen.Home.route
                DetailScreen(navController, from)
            }
            composable(Screen.LoginEmail.route) {
                LoginEmailScreen(
                    email = loginViewModel.loginData.collectAsState().value.email,
                    onEmailChange = { loginViewModel.updateEmail(it) },
                    submitEmail = { loginViewModel.submitEmail() },
                    moveToNextScreen = { navController.navigate(Screen.LoginOtp.route) },
                    enabled = loginViewModel.loginData.collectAsState().value.emailValid,
                    loginState = loginViewModel.loginData.collectAsState().value.loginState,
                    resetErrorState = { loginViewModel.resetErrorState() }
                )
            }
            composable(Screen.LoginOtp.route) {
                LoginOtpScreen(
                    email = loginViewModel.loginData.collectAsState().value.email,
                    otp = loginViewModel.loginData.collectAsState().value.otp,
                    onOtpChange = { loginViewModel.updateOtp(it) },
                    submitOtp = { loginViewModel.submitOtp() },
                    loginState = loginViewModel.loginData.collectAsState().value.loginState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ホーム画面", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.navigate("detail/${Screen.Home.route}") }) {
            Text("詳細画面へ")
        }
    }
}

@Composable
fun SearchScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "記録画面", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.navigate("detail/${Screen.Record.route}") }) {
            Text("詳細画面へ")
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "プロフィール画面", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.navigate("detail/${Screen.Profile.route}") }) {
            Text("詳細画面へ")
        }
    }
}

@Composable
fun DetailScreen(navController: NavController, from: String = "不明") {
    val fromScreenName = when (from) {
        Screen.Home.route -> "ホーム"
        Screen.Record.route -> "記録"
        Screen.Profile.route -> "プロフィール"
        else -> "不明"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "詳細画面", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "遷移元: $fromScreenName", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("前の画面に戻る")
        }
    }
}