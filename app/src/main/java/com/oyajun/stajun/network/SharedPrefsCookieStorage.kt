package com.oyajun.stajun.network

import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.util.date.GMTDate

class SharedPrefsCookieStorage(
    private val prefs: SharedPreferences
) : CookiesStorage {
    private val COOKIE_KEY = "cookies"

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        val set = prefs.getStringSet(COOKIE_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()

        // name, domain, pathで一意識別
        val cookieKey = "${cookie.name}|${cookie.domain ?: requestUrl.host}|${cookie.path ?: "/"}"

        // 既存の同じキーのcookieを削除
        set.removeAll { storedCookie ->
            val parts = storedCookie.split("|", limit = 4)
            if (parts.size >= 3) {
                val storedKey = "${parts[0]}|${parts[1]}|${parts[2]}"
                storedKey == cookieKey
            } else {
                false
            }
        }

        // 有効なcookieのみ保存
        if (!isExpired(cookie)) {
            val cookieString = "${cookieKey}|${cookie.toString()}"
            set.add(cookieString)
        }

        prefs.edit { putStringSet(COOKIE_KEY, set) }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        val set = prefs.getStringSet(COOKIE_KEY, emptySet()) ?: emptySet()
        val validCookies = mutableSetOf<String>()

        val cookies = set.mapNotNull { storedCookie ->
            val parts = storedCookie.split("|", limit = 4)
            if (parts.size >= 4) {
                val name = parts[0]
                val domain = parts[1]
                val path = parts[2]
                val cookieHeader = parts[3]

                try {
                    val cookie = parseServerSetCookieHeader(cookieHeader)
                    if (cookie != null &&
                        !isExpired(cookie) &&
                        matchesDomain(requestUrl.host, domain) &&
                        matchesPath(requestUrl.encodedPath, path)) {
                        validCookies.add(storedCookie)
                        cookie
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

        // 期限切れのcookieをクリーンアップ
        if (validCookies.size != set.size) {
            prefs.edit { putStringSet(COOKIE_KEY, validCookies) }
        }

        return cookies
    }

    private fun isExpired(cookie: Cookie): Boolean {
        val expires = cookie.expires
        return expires != null && expires.timestamp < GMTDate().timestamp
    }

    private fun matchesDomain(requestHost: String, cookieDomain: String): Boolean {
        return when {
            cookieDomain.startsWith(".") -> {
                requestHost == cookieDomain.substring(1) || requestHost.endsWith(cookieDomain)
            }
            else -> {
                requestHost == cookieDomain || requestHost.endsWith(".$cookieDomain")
            }
        }
    }

    private fun matchesPath(requestPath: String, cookiePath: String): Boolean {
        return requestPath.startsWith(cookiePath)
    }

    override fun close() {
        // クリーンアップ処理は不要
    }
}
