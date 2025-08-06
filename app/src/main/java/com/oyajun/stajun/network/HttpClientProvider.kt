package com.oyajun.stajun.network

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

object HttpClientProvider {
    @Volatile
    private var INSTANCE: HttpClient? = null

    fun getClient(context: Context): HttpClient {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: createClient(context).also { INSTANCE = it }
        }
    }

    private fun createClient(context: Context): HttpClient {
        return HttpClient(CIO) {
            install(HttpCookies) {
                val prefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
                storage = SharedPrefsCookieStorage(prefs)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    fun closeClient() {
        INSTANCE?.close()
        INSTANCE = null
    }
}
