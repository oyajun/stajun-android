package com.oyajun.stajun.network

import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader

class SharedPrefsCookieStorage(
    private val prefs: SharedPreferences
) : CookiesStorage {
    private val COOKIE_KEY = "cookies"

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        val set = prefs.getStringSet(COOKIE_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        // remove existing cookie with same name and domain/path
        set.removeAll { it.startsWith("${cookie.name}=") }
        set.add(cookie.toString())
        prefs.edit { putStringSet(COOKIE_KEY, set) }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val set = prefs.getStringSet(COOKIE_KEY, emptySet()) ?: emptySet()
        return set.mapNotNull { header ->
            parseServerSetCookieHeader(header)
        }.filter { cookie ->
            cookie.matches(requestUrl)
        }
    }

    override fun close() {
    }
}
