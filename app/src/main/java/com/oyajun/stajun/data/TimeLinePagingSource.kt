package com.oyajun.stajun.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.oyajun.stajun.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
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
    val originalBookId: BigInteger? = null,
    val createdAt: String,
    val updatedAt: String,
    val originalBook: Any? = null,
    val user: Any? = null
)


class TimeLinePagingSource (
    private val client: HttpClient
) : PagingSource<BigInteger, Record>() {

    override suspend fun load(params: LoadParams<BigInteger>): LoadResult<BigInteger, Record> {
        try {
            Log.d("TimeLinePaging", "接続試行先: ${BuildConfig.API_BASE_URL}")
            val response: HttpResponse = client.get(
                BuildConfig.API_BASE_URL + "/api/v1/record?cursor=${params.key ?: ""}&limit=${params.loadSize}"
            )
            val data = response.body<List<Record>>()

            val cursor = BigInteger(data.last().id)
            return LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = cursor
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<BigInteger, Record>): BigInteger? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { BigInteger(it) }
        }
    }
}