package com.example.filetransfer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        startActivity(Intent(this, Server::class.java))
    }
}