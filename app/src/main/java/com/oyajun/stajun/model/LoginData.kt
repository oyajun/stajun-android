package com.oyajun.stajun.model

data class LoginData(
    val email: String = "",
    val emailValid: Boolean = false,
    val otp: String = "",
    val loginState: LoginState = LoginState.EMAIL_IDLE
)

enum class LoginState {
    EMAIL_IDLE,
    EMAIL_LOADING,
    EMAIL_SUCCESS,
    EMAIL_ERROR,
    OTP_IDLE,
    OTP_LOADING,
    OTP_SUCCESS,
    OTP_ERROR,
}

