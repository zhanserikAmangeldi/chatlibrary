package com.example.chatlibrary.internal.models

internal data class ChatMessage(
    val content: String,
    val isSentByUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
