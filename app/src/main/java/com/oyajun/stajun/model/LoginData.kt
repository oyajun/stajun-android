package com.oyajun.stajun.model

data class LoginData(
    val email: String = "",
    val otp: String = "",
    val step: LoginStep = LoginStep.EMAIL,
    val loginState: LoginState = LoginState.IDLE
)

enum class LoginStep {
    EMAIL,
    OTP
}

enum class LoginState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

