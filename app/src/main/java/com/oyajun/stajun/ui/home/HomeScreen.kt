package com.oyajun.stajun.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel()
    val lazyPagingItems = viewModel.paging.collectAsLazyPagingItems()
    val pagingLoadStates = lazyPagingItems.loadState.source

    val refreshLoadState = pagingLoadStates.refresh  // 更新時
    val prependLoadState = pagingLoadStates.prepend  // 前データを読み込む時
    val appendLoadState = pagingLoadStates.append    // 後データを読み込む時

    when {
        refreshLoadState is LoadState.Error || prependLoadState is LoadState.Error || appendLoadState is LoadState.Error -> {
            // 何かしらのエラーが発生した
            Text(
                text = "Error: ",
                style = MaterialTheme.typography.bodyLarge
            )
            val errorMessage = when {
                refreshLoadState is LoadState.Error -> refreshLoadState.error.message ?: "Unknown error"
                prependLoadState is LoadState.Error -> prependLoadState.error.message ?: "Unknown error"
                appendLoadState is LoadState.Error -> appendLoadState.error.message ?: "Unknown error"
                else -> "Unknown error"
            }
        }
        refreshLoadState is LoadState.Loading -> {
            // 初期ローディング中または再更新でのローディング中
            Text("Loading...", style = MaterialTheme.typography.bodyLarge)
        }
        refreshLoadState is LoadState.NotLoading -> {
            // データを取得できた
            LazyColumn {
                if (prependLoadState is LoadState.Loading) {
                    // 前データを読み込み中
                    item {
                        Text("Loading previous data...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                items(lazyPagingItems.itemCount) { index ->
                    val record = lazyPagingItems[index]
                    record?.let {
                        Text(
                            text = it.id,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = it.comment ?: "No comment",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Amount: ${it.amount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Date: ${it.dateLocal}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }


                if (appendLoadState is LoadState.Loading) {
                    // 後データを読み込み中
                    item {
                        Text("Loading more data...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}