package com.example.unisehat.temu5

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unisehat.R

class ToastDemoActivity : AppCompatActivity() {

    private lateinit var context: Context
    private lateinit var toast: Toast
    private lateinit var b1: Button
    private var duration: Int = Toast.LENGTH_LONG
    private lateinit var myToast: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_toast_demo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        b1 = findViewById(R.id.b1)
        context = applicationContext
        myToast = "Hello World"
        toast = Toast.makeText(context, myToast, duration)

        b1.setOnClickListener { toast.show() }
    }
}
