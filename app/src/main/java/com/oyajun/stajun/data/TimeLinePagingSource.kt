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
import java.math.BigInteger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GoogleBooksResponse(
    val items: List<GoogleBookItem>? = null
)

@Serializable
data class GoogleBookItem(
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val title: String,
    val imageLinks: ImageLinks? = null
)

@Serializable
data class ImageLinks(
    val thumbnail: String? = null
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

            // レコードリストを変更可能にするためにmutableListに変換
            val mutableData = data.toMutableList()

            mutableData.forEachIndexed { index, record ->
                if (record.type == "PUBLISHED_BOOK" && record.publishedBook == null) {
                    // Google Books APIにアクセスして書籍情報を取得
                    record.isbn?.let { isbn ->
                        try {
                            val googleBooksUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:$isbn"
                            Log.d("TimeLinePaging", "Google Books API 接続試行先: $googleBooksUrl")
                            val googleBooksResponse: HttpResponse = client.get(googleBooksUrl)
                            if (googleBooksResponse.status.isSuccess()) {
                                val responseText = googleBooksResponse.body<String>()
                                Log.d("TimeLinePaging", "Google Books API レスポンス (ISBN: $isbn): $responseText")

                                val json = Json { ignoreUnknownKeys = true }
                                val googleBooksData = json.decodeFromString<GoogleBooksResponse>(responseText)

                                googleBooksData.items?.firstOrNull()?.let { bookItem ->
                                    val publishedBook = PublishedBook(
                                        title = bookItem.volumeInfo.title,
                                        imageURL = bookItem.volumeInfo.imageLinks?.thumbnail ?: ""
                                    )

                                    // 新しいRecordオブジェクトを作成（publishedBookを設定）
                                    mutableData[index] = record.copy(publishedBook = publishedBook)
                                    Log.d("TimeLinePaging", "書籍情報を設定: ${publishedBook.title}")
                                }
                            } else {
                                Log.w("TimeLinePaging", "Google Books API エラー (ISBN: $isbn): ${googleBooksResponse.status}")
                            }
                        } catch (e: Exception) {
                            Log.e("TimeLinePaging", "Google Books API呼び出しエラー (ISBN: $isbn)", e)
                        }
                    }
                }
            }

            val cursor = if (mutableData.isNotEmpty()) BigInteger(mutableData.last().id) else null
            return LoadResult.Page(
                data = mutableData,
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