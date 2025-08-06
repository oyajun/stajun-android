package com.oyajun.stajun.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.oyajun.stajun.data.Record
import com.oyajun.stajun.data.TimeLineList
import com.oyajun.stajun.network.HttpClientProvider
import kotlinx.coroutines.flow.Flow

class HomeViewModel (application: Application) : AndroidViewModel(application){

    private val client = HttpClientProvider.getClient(getApplication())

    val paging: Flow<PagingData<Record>> = TimeLineList.getRecords(client, getApplication()).cachedIn(viewModelScope)
}