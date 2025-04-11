package com.example.chatlibrary.internal

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

internal class WebSocketManager {
    private val WEBSOCKET_URL = "wss://echo.websocket.org/"
    private val SPECIAL_MESSAGE = "203 = 0xcb"
    private val SPECIAL_MESSAGE_REPLACEMENT = "This is a predefined message replacing the original data."

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var messageListener: MessageListener? = null

    internal interface MessageListener {
        fun onMessageReceived(message: String)
        fun onConnectionEstablished()
        fun onConnectionClosed(reason: String)
        fun onConnectionError(error: String)
    }

    fun connect(listener: MessageListener) {
        messageListener = listener

        val request = Request.Builder()
            .url(WEBSOCKET_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened: ${response.code}")
                messageListener?.onConnectionEstablished()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received text message: $text")
                if (text == SPECIAL_MESSAGE) {
                    messageListener?.onMessageReceived(SPECIAL_MESSAGE_REPLACEMENT)
                } else {
                    messageListener?.onMessageReceived(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                messageListener?.onConnectionClosed("Connection closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                messageListener?.onConnectionClosed("Connection closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection error: ${t.message}, Response: ${response?.code}")
                messageListener?.onConnectionError("Connection error: ${t.message}")
            }
        })
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun disconnect() {
        webSocket?.close(1000, "User closed the chat")
        webSocket = null
        messageListener = null
        client.dispatcher.executorService.shutdown()
    }
}