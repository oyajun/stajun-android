package com.oyajun.stajun.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Record(){
    Row {
        Text("aaaaaa")
        Column {
            Text("bbbbbb")
            Text("cccccc")
        }
    }
}

@Preview
@Composable
fun RecordPreview() {
    Record()
}