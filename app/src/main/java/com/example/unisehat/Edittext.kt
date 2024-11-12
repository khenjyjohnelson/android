package com.example.unisehat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Edittext : AppCompatActivity() {

    private lateinit var editTextNama: EditText
    private lateinit var editTextGajiPokok: EditText
    private lateinit var editTextTunjangan: EditText
    private lateinit var textViewHasilNama: TextView
    private lateinit var textViewTotalGaji: TextView
    private lateinit var trims: TextView
    private lateinit var buttonHitung: Button
    private lateinit var resetButton: Button

    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edittext)

        editTextNama = findViewById(R.id.editTextNama)
        editTextGajiPokok = findViewById(R.id.editTextGajiPokok)
        editTextTunjangan = findViewById(R.id.editTextTunjangan)
        textViewTotalGaji = findViewById(R.id.textViewTotalGaji)
        textViewHasilNama = findViewById(R.id.textViewHasilNama)
        buttonHitung= findViewById(R.id.buttonHitung)
        resetButton = findViewById(R.id.buttonReset)

        trims = findViewById(R.id.terimaKasih)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        buttonHitung.setOnClickListener {
            val nama = editTextNama.text.toString()
            val gajiPokok = editTextGajiPokok.text.toString().toDouble()
            val tunjangan = editTextTunjangan.text.toString().toDouble()

            // Hitung total gaji dengan mengurangi pajak 5%
            val totalGaji = gajiPokok + tunjangan - (gajiPokok * 0.05)

            // Tampilkan total gaji
            textViewHasilNama.setText(nama.toString())
            textViewTotalGaji.setText(totalGaji.toString())

            trims.setText("Terima Kasih telah menggunakan Aplikasi Ini!")
        }


        // If you want to reset the values, you can add an OnClickListener for the "Reset" button.

        resetButton.setOnClickListener { // Reset the values or perform any necessary actions.
            editTextNama.setText("")
            editTextGajiPokok.setText("")
            editTextTunjangan.setText("")
            textViewHasilNama.setText("")
            textViewTotalGaji.setText("")
        }

        back = findViewById(R.id.backButton)
        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}