package com.oyajun.stajun.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.oyajun.stajun.data.Record
import com.oyajun.stajun.data.TimeLineList
import com.oyajun.stajun.network.SharedPrefsCookieStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import kotlinx.coroutines.flow.Flow

class HomeViewModel (application: Application) : AndroidViewModel(application){

    private val client = HttpClient(CIO) {
        install(HttpCookies) {
            val prefs = getApplication<Application>().getSharedPreferences("cookies", Context.MODE_PRIVATE)
            storage = SharedPrefsCookieStorage(prefs)
        }
    }

    val paging: Flow<PagingData<Record>> = TimeLineList.getRecords( client).cachedIn(viewModelScope)
}