package com.example.filetransfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket

class MainApp : AppCompatActivity() {
    lateinit var outputStream: OutputStream
    lateinit var inputStream: InputStream
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_app)
        val extras = intent.extras!!
        val socket = extras.get("socket") as MySocket
        outputStream = socket.socket.getOutputStream()
        inputStream = socket.socket.getInputStream()
    }
}
