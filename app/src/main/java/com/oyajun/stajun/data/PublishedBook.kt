package com.oyajun.stajun.data

import kotlinx.serialization.Serializable

@Serializable
data class PublishedBook(
    val title: String,
    val imageURL: String,
)