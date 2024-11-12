package com.example.unisehat.temu4

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unisehat.R

class RadioButtonActivity : AppCompatActivity() {
    private lateinit var list_opsi: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio_button)

        list_opsi = findViewById(R.id.opsi)
        list_opsi.setOnCheckedChangeListener { radioGroup, id ->
            when (id) {
                R.id.java -> Toast.makeText(applicationContext, "Saya Suka Java", Toast.LENGTH_SHORT).show()
                R.id.kotlin -> Toast.makeText(applicationContext, "Saya Suka Kotlin", Toast.LENGTH_SHORT).show()
                R.id.cpp -> Toast.makeText(applicationContext, "Saya Suka C++", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
