package com.oyajun.stajun

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oyajun.stajun.ui.login.LoginViewModel
import com.oyajun.stajun.ui.theme.StaJunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StaJunTheme {
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) } // null = 初期化中
                val loginViewModel: LoginViewModel = viewModel()

                // 初期化処理でログイン状態をチェック
                LaunchedEffect(Unit) {
                    // cookieからログイン状態を確認
                    val prefs = getSharedPreferences("cookies", MODE_PRIVATE)
                    val cookieSet = prefs.getStringSet("cookies", emptySet()) ?: emptySet()

                    // デバッグ用ログ
                    Log.d("MainActivity", "保存されているcookie数: ${cookieSet.size}")
                    cookieSet.forEach { cookie ->
                        Log.d("MainActivity", "Cookie: $cookie")
                    }

                    // cookieからauth_tokenまたはセッション情報を確認
                    val hasAuthCookie = cookieSet.any { cookie ->
                        // name|domain|path|cookie_header 形式で保存されているcookieを確認
                        val parts = cookie.split("|", limit = 4)
                        if (parts.size >= 4) {
                            val name = parts[0]
                            val cookieHeader = parts[3]

                            // より詳細なcookieチェック
                            val isAuthCookie = name == "auth_token" ||
                                             name == "session_id" ||
                                             name == "access_token" ||
                                             name == "sessionid" ||
                                             name == "jwt" ||
                                             cookieHeader.contains("auth", ignoreCase = true) ||
                                             cookieHeader.contains("session", ignoreCase = true)

                            Log.d("MainActivity", "Cookie名: $name, 認証Cookie: $isAuthCookie")
                            isAuthCookie
                        } else {
                            false
                        }
                    }

                    Log.d("MainActivity", "ログイン状態: $hasAuthCookie")
                    isLoggedIn = hasAuthCookie
                }

                when (isLoggedIn) {
                    null -> {
                        // 初期化中：スプラッシュ画面やローディング画面
                        SplashScreen()
                    }
                    true -> {
                        // ログイン済み：メインアプリ
                        StaJunApp(initialLoginState = true)
                    }
                    false -> {
                        // 未ログイン：ログインフロー
                        StaJunApp(initialLoginState = false)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}