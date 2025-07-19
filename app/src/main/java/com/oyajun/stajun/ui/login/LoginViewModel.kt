package com.oyajun.stajun.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyajun.stajun.BuildConfig
import com.oyajun.stajun.model.LoginData
import com.oyajun.stajun.model.LoginState
import com.oyajun.stajun.network.SharedPrefsCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        _loginData.value = _loginData.value.copy(
            email = email,
            emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            loginState = LoginState.EMAIL_IDLE // エラー状態をリセット
        )
    }

    fun updateOtp(otp: String) {
        _loginData.value = _loginData.value.copy(
            otp = otp,
            loginState = if (_loginData.value.loginState == LoginState.OTP_ERROR) LoginState.OTP_IDLE else _loginData.value.loginState // エラー状態をリセット
        )
    }

    fun submitEmail() {
        viewModelScope.launch {
            _loginData.value = _loginData.value.copy(loginState = LoginState.EMAIL_LOADING)
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
                    _loginData.value = _loginData.value.copy(loginState = LoginState.EMAIL_SUCCESS)
                } else {
                    _loginData.value = _loginData.value.copy(loginState = LoginState.EMAIL_ERROR)
                }
            } catch (e: Exception) {
                // logcat
                Log.e("LoginViewModel", "📫Error sending email OTP", e)
                println("Error sending email OTP: ${e.message}")
                _loginData.value = _loginData.value.copy(loginState = LoginState.EMAIL_ERROR)
            }
        }
    }

    fun submitOtp() {
        viewModelScope.launch {
            _loginData.value = _loginData.value.copy(loginState = LoginState.OTP_LOADING)
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
                    // cookieが正しく保存されているかチェック
                    val prefs = getApplication<Application>().getSharedPreferences("cookies", Context.MODE_PRIVATE)
                    val cookieSet = prefs.getStringSet("cookies", emptySet()) ?: emptySet()
                    Log.d("LoginViewModel", "ログイン成功後のcookie数: ${cookieSet.size}")
                    cookieSet.forEach { cookie ->
                        Log.d("LoginViewModel", "保存されたCookie: $cookie")
                    }

                    _loginData.value = _loginData.value.copy(loginState = LoginState.OTP_SUCCESS)
                } else {
                    _loginData.value = _loginData.value.copy(loginState = LoginState.OTP_ERROR)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "📫Error sending email OTP", e)
                println("Error sending email OTP: ${e.message}")
                _loginData.value = _loginData.value.copy(loginState = LoginState.OTP_ERROR)
            }
        }
    }

    // エラー状態を手動でリセットする関数を追加
    fun resetErrorState() {
        when (_loginData.value.loginState) {
            LoginState.EMAIL_ERROR -> _loginData.value = _loginData.value.copy(loginState = LoginState.EMAIL_IDLE)
            LoginState.OTP_ERROR -> _loginData.value = _loginData.value.copy(loginState = LoginState.OTP_IDLE)
            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
