package com.oyajun.stajun.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

object TimeLineList {
    fun getRecords(client: HttpClient): Flow<PagingData<Record>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,  // 初期取得数、ページサイズを10件に設定。
                pageSize = 10
            )
        ) {
            TimeLinePagingSource(
                client = client
            )
        }.flow
    }
}