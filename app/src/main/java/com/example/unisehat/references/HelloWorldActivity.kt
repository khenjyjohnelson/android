package com.example.unisehat.references

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unisehat.R

class HelloWorldActivity : AppCompatActivity() {

    private lateinit var mChangeTextButton: Button
    private lateinit var mHelloTextView: TextView

    private val KEY_TEXT = "uiText"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

        // Initialize views
        mHelloTextView = findViewById(R.id.tv_hello)
        mChangeTextButton = findViewById(R.id.bt_change)

        // Set click listeners
        mChangeTextButton.setOnClickListener {
            mHelloTextView.text = "Welcome!" // Change text directly
        }

        mChangeTextButton.setOnClickListener {
            mHelloTextView.text = "Welcome!" // This block is redundant, so removed it
        }

        mChangeTextButton.setOnClickListener {
            mHelloTextView.text = getText(R.string.welcome_text)
        }

        // Restore text if there's a saved instance state
        if (savedInstanceState != null) {
            mHelloTextView.text = savedInstanceState.getString(KEY_TEXT, getString(R.string.hello_text))
        }

        // Adjusting padding with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TEXT, mHelloTextView.text.toString())
        super.onSaveInstanceState(outState)
    }
}
