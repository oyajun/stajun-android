package com.oyajun.stajun.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oyajun.stajun.BuildConfig
import com.oyajun.stajun.model.LoginData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.oyajun.stajun.model.LoginState
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*

class LoginViewModel : ViewModel() {
    private val _loginData = MutableStateFlow(LoginData())
    val loginData: StateFlow<LoginData> = _loginData.asStateFlow()

    fun updateEmail(email: String) {
        _loginData.value = _loginData.value.copy(email = email)
        _loginData.value = _loginData.value.copy(
            emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        )
    }

    fun updateOtp(otp: String) {
        _loginData.value = _loginData.value.copy(otp = otp)
    }

    fun submitEmail() {
        viewModelScope.launch {
            _loginData.value = _loginData.value.copy(loginState = LoginState.LOADING)
            _loginData.value = _loginData.value.copy(otp = "")
            val client = HttpClient(CIO)
            try {
                Log.d("LoginViewModel", "接続試行先: ${BuildConfig.API_BASE_URL}")
                val response: HttpResponse = client.post(
                    BuildConfig.API_BASE_URL + "/auth/email-otp/send-verification-otp"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody("""{"email":"${_loginData.value.email}","type":"sign-in"}""")
                }
                Log.d("LoginViewModel", "OTP リクエスト成功: ${response.status}")
                if (response.status.isSuccess()) {
                    _loginData.value = _loginData.value.copy(loginState = LoginState.SUCCESS)
                } else {
                    _loginData.value = _loginData.value.copy(loginState = LoginState.ERROR)
                }
            } catch (e: Exception) {
                // logcat
                Log.e("LoginViewModel", "📫Error sending email OTP", e)
                println("Error sending email OTP: ${e.message}")
                _loginData.value = _loginData.value.copy(loginState = LoginState.ERROR)
            } finally {
                client.close()
            }
        }
    }

    fun submitOtp() {
        viewModelScope.launch {
            _loginData.value = _loginData.value.copy(loginState = LoginState.LOADING)
            // TODO: Implement OTP verification logic
            // For now, just simulate success
            _loginData.value = _loginData.value.copy(loginState = LoginState.SUCCESS)
        }
    }
}
