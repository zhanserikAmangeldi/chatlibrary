package com.example.chatlibrary.internal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatlibrary.R
import com.example.chatlibrary.internal.models.ChatMessage


internal class ChatActivity : AppCompatActivity(), WebSocketManager.MessageListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button

    private val messageAdapter = MessageAdapter()
    private val webSocketManager = WebSocketManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat"

        recyclerView = findViewById(R.id.recycler_chat)
        messageInput = findViewById(R.id.edit_message)
        sendButton = findViewById(R.id.button_send)

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.text.clear()
            }
        }

        webSocketManager.connect(this)
    }

    private fun sendMessage(message: String) {
        messageAdapter.addMessage(ChatMessage(message, true))
        scrollToBottom()

        val success = webSocketManager.sendMessage(message)
        if (!success) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToBottom() {
        recyclerView.post {
            val itemCount = messageAdapter.itemCount
            if (itemCount > 0) {
                recyclerView.smoothScrollToPosition(itemCount - 1)
            }
        }
    }

    override fun onMessageReceived(message: String) {
        runOnUiThread {
            messageAdapter.addMessage(ChatMessage(message, false))
            scrollToBottom()
        }
    }

    override fun onConnectionEstablished() {
        runOnUiThread {
            Toast.makeText(this, "Connected to chat server", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionClosed(reason: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection closed: $reason", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        webSocketManager.disconnect()
        super.onDestroy()
    }
}