package com.oyajun.stajun.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.oyajun.stajun.data.Record
import com.oyajun.stajun.data.User

@Composable
fun RecordCard(
    record: Record,
    modifier: Modifier = Modifier
){
    Column (){
        Row (
        ){
            Text(
                text = record.user?.name ?: "Unknown User",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = record.dateLocal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text("${record.amount} min.")
        Text("${record.comment}")
    }
}

@Preview
@Composable
fun RecordCardPreview() {
    val user = User(name = "OyaJun")
    val record = Record(
        id = "1",
        userId = "user123",
        user = user,
        type = "typeA",
        amount = 100,
        comment = "Sample comment",
        dateUTC = "2023-10-01T12:00:00Z",
        dateLocal = "2023-10-01T21:00:00+09:00",
        isbn = "978-3-16-148410-0",
        originalBookId = "book123",
        createdAt = "2023-10-01T12:00:00Z",
        updatedAt = "2023-10-01T12:00:00Z"
    )
    RecordCard(
        record,
    )
}