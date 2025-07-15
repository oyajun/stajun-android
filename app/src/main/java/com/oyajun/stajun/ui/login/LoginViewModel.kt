package com.oyajun.stajun.ui.login

import android.util.Log
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
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
import io.ktor.client.plugins.cookies.HttpCookies
import com.oyajun.stajun.network.SharedPrefsCookieStorage

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val client = HttpClient(CIO) {
        install(HttpCookies) {
            val prefs = getApplication<Application>().getSharedPreferences("cookies", Context.MODE_PRIVATE)
            storage = SharedPrefsCookieStorage(prefs)
        }
    }

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
            try {
                Log.d("LoginViewModel", "接続試行先: ${BuildConfig.API_BASE_URL}")
                val response: HttpResponse = client.post(
                    BuildConfig.API_BASE_URL + "/api/auth/email-otp/send-verification-otp"
                ) {
                    contentType(ContentType.Application.Json)
                    setBody("""{"email":"${_loginData.value.email}","type":"sign-in"}""")
                }
                // cookies from Set-Cookie headers are now stored automatically
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
            }
        }
    }

    fun submitOtp() {
        viewModelScope.launch {
            _loginData.value = _loginData.value.copy(loginState = LoginState.LOADING)
            try {
                Log.d("LoginViewModel", "接続試行先: ${BuildConfig.API_BASE_URL}")
                val response: HttpResponse = client.post(
                    BuildConfig.API_BASE_URL + "/api/auth/sign-in/email-otp "
                ) {
                    contentType(ContentType.Application.Json)
                    setBody("""{"email":"${_loginData.value.email}","otp":"${_loginData.value.otp}"}""")
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
            }
            _loginData.value = _loginData.value.copy(loginState = LoginState.SUCCESS)
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
