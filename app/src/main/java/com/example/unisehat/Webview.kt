package com.example.unisehat

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.annotation.RequiresApi

class Webview : AppCompatActivity() {

    private lateinit var webview: WebView
    private lateinit var back: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webview = findViewById(R.id.webView)

        webviewsetup()

        back = findViewById(R.id.backButton)
        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun webviewsetup(){
        webview.webViewClient = WebViewClient()
        webview.apply {
            loadUrl("http://uvers.ac.id")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
        }
    }




}