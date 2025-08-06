package com.oyajun.stajun.data

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.oyajun.stajun.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class Record(
    val id: String,
    val userId: String,
    val type: String,
    val amount: Int,
    val comment: String? = null,
    val dateUTC: String,
    val dateLocal: String,
    val isbn: String? = null,
    val originalBookId: String? = null, // BigInteger → String に変更
    val createdAt: String,
    val updatedAt: String,
    val originalBook: OriginalBook? = null, // Any → 具体的な型に変更
    val user: User? = null // Any → 具体的な型に変更
)

@Serializable
data class OriginalBook(
    val id: String,
    val userId: String,
    val type: String,
    val order: Int,
    val status: String,
    val isbn: String? = null,
    val title: String,
    val color: String? = null,
    val icon: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class User(
    val name: String
)


class TimeLinePagingSource (
    private val client: HttpClient,
    private val context: Context
) : PagingSource<BigInteger, Record>() {

    override suspend fun load(params: LoadParams<BigInteger>): LoadResult<BigInteger, Record> {
        try {
            val url = BuildConfig.API_BASE_URL + "/api/v1/record?cursor=${params.key ?: ""}&limit=${params.loadSize}"
            Log.d("TimeLinePaging", "接続試行先: $url")

            // SharedPreferencesからクッキーの保存状況を確認
            val prefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
            val cookieSet = prefs.getStringSet("cookies", emptySet()) ?: emptySet()
            Log.d("TimeLinePaging", "保存されているクッキー数: ${cookieSet.size}")
            cookieSet.forEach { cookie ->
                Log.d("TimeLinePaging", "保存クッキー: $cookie")
            }

            val response: HttpResponse = client.get(url)
            Log.d("TimeLinePaging", "API レスポンス: ${response.status}")


            // レスポンスヘッダーも確認
            Log.d("TimeLinePaging", "=== RESPONSE HEADERS ===")
            response.headers.forEach { name, values ->
                Log.d("TimeLinePaging", "Response header: $name = ${values.joinToString(", ")}")
            }

            // 401エラーの場合はエラーメッセージを確認
            if (response.status.value == 401) {
                val errorBody = response.body<String>()
                Log.e("TimeLinePaging", "401 Unauthorized - Response body: $errorBody")
                return LoadResult.Error(Exception("認証エラー: $errorBody"))
            }

            // 成功でない場合も詳���を確認
            if (!response.status.isSuccess()) {
                val errorBody = response.body<String>()
                Log.e("TimeLinePaging", "${response.status.value} Error - Response body: $errorBody")
                return LoadResult.Error(Exception("API エラー (${response.status.value}): $errorBody"))
            }

            val data = response.body<List<Record>>()
            Log.d("TimeLinePaging", "取得したレコード数: ${data.size}")

            val cursor = if (data.isNotEmpty()) BigInteger(data.last().id) else null
            return LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = cursor
            )
        } catch (e: Exception) {
            Log.e("TimeLinePaging", "API呼び出しエラー", e)
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<BigInteger, Record>): BigInteger? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { BigInteger(it) }
        }
    }
}