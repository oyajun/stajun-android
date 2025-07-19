package com.oyajun.stajun.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oyajun.stajun.model.LoginState

@Composable
fun LoginOtpScreen(
    email : String,
    otp: String,
    onOtpChange: (String) -> Unit,
    submitOtp: () -> Unit,
    loginState: LoginState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "ワンタイムパスワードを入力してください", style = MaterialTheme.typography.headlineLarge)
        Text(text = "$email に送信しました", style = MaterialTheme.typography.bodyLarge)
        Text(text = "迷惑メールも確認してください", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("ワンタイムパスワード") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = submitOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = otp.isNotBlank() && loginState != LoginState.OTP_LOADING
        ) {
            Text("認証")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
        when (loginState) {
            LoginState.OTP_LOADING -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
            LoginState.OTP_ERROR -> {
                Text("エラー", color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}

@Preview
@Composable
fun LoginOtpScreenPreview() {
    LoginOtpScreen(
        email = "example@example.com",
        otp = "",
        onOtpChange = {},
        loginState = LoginState.OTP_IDLE,
        onBack = {},
        submitOtp = {}
    )
}