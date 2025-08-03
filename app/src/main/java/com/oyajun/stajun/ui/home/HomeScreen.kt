package com.oyajun.stajun.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen() {
    LazyColumn {
        items(1000000000) { index ->
            // ここに各アイテムのコンテンツを配置
            // 例えば、Text("Item $index")など
            Text(
                text = "Item $index",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}