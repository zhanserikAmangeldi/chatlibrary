package com.example.chatlibrary

import android.content.Context
import android.content.Intent
import com.example.chatlibrary.internal.ChatActivity

class ChatLibrary {
    companion object {
        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, ChatActivity::class.java)
            context.startActivity(intent)
        }
    }
}