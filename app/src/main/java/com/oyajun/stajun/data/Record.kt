package com.oyajun.stajun.data

import kotlinx.serialization.Serializable

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
    val originalBookId: String? = null, // BigInteger → String に変更
    val createdAt: String,
    val updatedAt: String,
    val originalBook: OriginalBook? = null, // Any → 具体的な型に変更
    val publishedBook: PublishedBook? = null, // Any → 具体的な型に変更
    val user: User? = null // Any → 具体的な型に変更
)