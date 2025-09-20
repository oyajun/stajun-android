package com.oyajun.stajun.data

import kotlinx.serialization.Serializable

@Serializable
data class OriginalBook(
    val id: String,
    val userId: String,
    val type: String,
    val order: Int,
    val status: String,
    val isbn: String? = null,
    val title: String,
    val color: String? = null,
    val icon: String? = null,
    val createdAt: String,
    val updatedAt: String
)