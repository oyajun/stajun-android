package com.oyajun.stajun.network

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import java.net.URLDecoder
import java.net.URLEncoder

class SharedPrefsCookieStorage(
    private val prefs: SharedPreferences
) : CookiesStorage {
    private val COOKIE_KEY = "cookies"

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        Log.d("CookieStorage", "addCookie called for URL: $requestUrl")
        Log.d("CookieStorage", "Cookie to add: name=${cookie.name}, value=${cookie.value}, domain=${cookie.domain}, path=${cookie.path}, expires=${cookie.expires}")

        val set = prefs.getStringSet(COOKIE_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()

        // name, domain, pathで一意識別
        val cookieDomain = cookie.domain ?: requestUrl.host
        val cookiePath = cookie.path ?: "/"
        val cookieKey = "${cookie.name}|${cookieDomain}|${cookiePath}"

        // 既存の同じキーのcookieを削除
        set.removeAll { storedCookie ->
            storedCookie.startsWith("$cookieKey|")
        }

        // 有効なcookieのみ保存
        if (!isExpired(cookie)) {
            // クッキー値をURLデコードして保存（二重エンコード防止）
            val decodedValue = try {
                URLDecoder.decode(cookie.value, "UTF-8")
            } catch (e: Exception) {
                Log.w("CookieStorage", "Failed to decode cookie value, using as-is: ${cookie.value}")
                cookie.value
            }

            // シンプルな形式で保存: name|domain|path|value|expires|secure|httpOnly
            val cookieString = "${cookie.name}|${cookieDomain}|${cookiePath}|${decodedValue}|${cookie.expires?.timestamp ?: -1}|${cookie.secure}|${cookie.httpOnly}"
            set.add(cookieString)
            Log.d("CookieStorage", "Cookie saved (decoded): $cookieString")
        } else {
            Log.d("CookieStorage", "Cookie expired, not saving: ${cookie.name}")
        }

        prefs.edit { putStringSet(COOKIE_KEY, set) }
        Log.d("CookieStorage", "Total cookies stored: ${set.size}")
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        Log.d("CookieStorage", "get called for URL: $requestUrl")
        val set = prefs.getStringSet(COOKIE_KEY, emptySet()) ?: emptySet()
        Log.d("CookieStorage", "Stored cookies count: ${set.size}")

        val validCookies = mutableSetOf<String>()

        val cookies = set.mapNotNull { storedCookie ->
            Log.d("CookieStorage", "Checking stored cookie: $storedCookie")
            val parts = storedCookie.split("|")
            if (parts.size >= 7) {
                val name = parts[0]
                val domain = parts[1]
                val path = parts[2]
                val value = parts[3]
                val expiresTimestamp = parts[4].toLongOrNull() ?: -1
                val secure = parts[5].toBoolean()
                val httpOnly = parts[6].toBoolean()

                try {
                    val expires = if (expiresTimestamp > 0) GMTDate(expiresTimestamp) else null
                    val cookie = Cookie(
                        name = name,
                        value = value,
                        domain = domain,
                        path = path,
                        expires = expires,
                        secure = secure,
                        httpOnly = httpOnly
                    )

                    Log.d("CookieStorage", "Parsed cookie: name=$name, value=$value, domain=$domain, path=$path")
                    Log.d("CookieStorage", "Request: host=${requestUrl.host}, path=${requestUrl.encodedPath}")

                    if (!isExpired(cookie) &&
                        matchesDomain(requestUrl.host, domain) &&
                        matchesPath(requestUrl.encodedPath, path)) {
                        validCookies.add(storedCookie)
                        Log.d("CookieStorage", "Cookie matches and will be sent: name=$name, value=$value")
                        cookie
                    } else {
                        Log.d("CookieStorage", "Cookie does not match or expired: name=$name, domain=$domain, path=$path")
                        Log.d("CookieStorage", "  - expired: ${isExpired(cookie)}")
                        Log.d("CookieStorage", "  - domain match: ${matchesDomain(requestUrl.host, domain)}")
                        Log.d("CookieStorage", "  - path match: ${matchesPath(requestUrl.encodedPath, path)}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("CookieStorage", "Error parsing cookie: $storedCookie", e)
                    null
                }
            } else {
                Log.w("CookieStorage", "Invalid cookie format (${parts.size} parts): $storedCookie")
                null
            }
        }

        // 期限切れのcookieをクリーンアップ
        if (validCookies.size != set.size) {
            prefs.edit { putStringSet(COOKIE_KEY, validCookies) }
            Log.d("CookieStorage", "Cleaned up expired cookies. Valid: ${validCookies.size}, Previous: ${set.size}")
        }

        Log.d("CookieStorage", "Returning ${cookies.size} cookies for request")
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
