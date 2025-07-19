package com.oyajun.stajun.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oyajun.stajun.R
import com.oyajun.stajun.model.LoginState

@Composable
fun LoginEmailScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    submitEmail: () -> Unit,
    moveToNextScreen: () -> Unit = {},
    enabled: Boolean,
    loginState: LoginState,
    resetErrorState: () -> Unit = {},
) {
    val openAlertDialog = remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState == LoginState.EMAIL_SUCCESS) {
            moveToNextScreen()
        } else if (loginState == LoginState.EMAIL_ERROR) {
            openAlertDialog.value = true
        } else if (loginState == LoginState.EMAIL_LOADING) {

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Text(text = "ログイン / 新規登録", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "メールアドレスを入力してください", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("メールアドレス") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = submitEmail,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text("次へ")
        }
    }


    when {
        openAlertDialog.value -> {
            ErrorAlertDialog(
                onDismissRequest = {
                    openAlertDialog.value = false
                    resetErrorState()
                },
                icon = Icons.Default.Warning
            )
        }
    }
}

@Composable
fun ErrorAlertDialog(
    onDismissRequest: () -> Unit,
    icon: ImageVector,
) {

    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "警告アイコン")
        },
        title = {
            Text(text = "ワンタイムパスワードを送信できませんでした")
        },
        text = {
            Text(text = "もう一度試してください")
        },

        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("閉じる")
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@Preview
@Composable
fun LoginEmailScreenPreview() {
    LoginEmailScreen(
        email = "",
        onEmailChange = {},
        submitEmail = {},
        enabled = false,
        loginState = LoginState.EMAIL_IDLE
    )
}

@Preview
@Composable
fun ErrorAlertDialogPreview() {
    ErrorAlertDialog(
        onDismissRequest = {},
        icon = Icons.Default.Warning // Replace with your icon
    )
}