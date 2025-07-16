package com.oyajun.stajun

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// ナビゲーションルートの定義
sealed class Screen(
    val route: String,
    val title: String,
    val icon: @Composable () -> Unit,
    val showNavigationBar: Boolean = true // NavigationBarの表示フラグを追加
) {
    object Home : Screen("home", "ホーム", { Icon(Icons.Filled.Home, contentDescription = "ホーム") })
    object Search : Screen("search", "検索", { Icon(Icons.Filled.Search, contentDescription = "検索") })
    object Profile : Screen("profile", "プロフィール", { Icon(Icons.Filled.Person, contentDescription = "プロフィール") })
    object Detail : Screen("detail", "詳細", { /* 詳細画面にはアイコンは不要 */ }, showNavigationBar = true) // NavigationBarを表示に変更
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaJunApp() {
    val navController = rememberNavController()
    // NavigationBarで表示するアイテムのリスト（NavigationBarに表示するものだけ）
    val navBarItems = listOf(Screen.Home, Screen.Search, Screen.Profile)

    // 全ての画面の定義（NavigationBarの表示設定を含む）
    val allScreens = listOf(Screen.Home, Screen.Search, Screen.Profile, Screen.Detail)

    // NavigationBarの表示/非表示を制御するための現在のルートを取得
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 現在の画面でNavigationBarを表示するかどうかを判断
    val shouldShowNavigationBar = if (currentRoute?.startsWith("detail") == true) {
        // 詳細画面の場合はtrueを返す（Detail画面のshowNavigationBarがtrueなので）
        Screen.Detail.showNavigationBar
    } else {
        val currentScreen = allScreens.find { it.route == currentRoute }
        currentScreen?.showNavigationBar ?: false
    }

    // 詳細画面の場合、前の画面（遷移元）をパラメータから取得
    val selectedRoute = if (currentRoute?.startsWith("detail") == true) {
        // 詳細画面のパラメータから前の画面を取得
        navBackStackEntry?.arguments?.getString("from") ?: Screen.Home.route
    } else {
        currentRoute
    }

    Scaffold(
        bottomBar = {
            if (shouldShowNavigationBar) {
                NavigationBar {
                    navBarItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { item.icon() },
                            label = { Text(item.title) },
                            selected = selectedRoute == item.route, // 詳細画面では前の画面を選択状態に
                            onClick = {
                                navController.navigate(item.route) {
                                    // ナビゲーションバーのアイテムを押したときに、
                                    // 該当するルートまでのバックスタックをポップし、新しいルートに遷移する。
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true // 同じルートへの重複ナビゲーションを防ぐ
                                    restoreState = true // 以前の保存された状態を復元する
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
            composable("detail/{from}") { backStackEntry ->
                val from = backStackEntry.arguments?.getString("from") ?: Screen.Home.route
                DetailScreen(navController)
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
        Text(text = "検索画面", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.navigate("detail/${Screen.Search.route}") }) {
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
fun DetailScreen(navController: NavController) {
    val from = navController.previousBackStackEntry?.destination?.route ?: "不明"
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "詳細画面", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "遷移元: $from", modifier = Modifier.padding(bottom = 16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("前の画面に戻る")
        }
    }
}
