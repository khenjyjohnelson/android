package com.example.unisehat.temu4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unisehat.R

class HitungEmasActivity : AppCompatActivity() {
    private lateinit var hargaEmasEditText: EditText
    private lateinit var nilaiKaratEditText: EditText
    private lateinit var jumlahEmasEditText: EditText
    private lateinit var upahPengrajinEditText: EditText
    private lateinit var textHarga: TextView
    private lateinit var textJumlahEmas: TextView
    private lateinit var textTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hitung_emas)

        hargaEmasEditText = findViewById(R.id.HargaEmas)
        nilaiKaratEditText = findViewById(R.id.NilaiKarat)
        jumlahEmasEditText = findViewById(R.id.JumlahEmas)
        upahPengrajinEditText = findViewById(R.id.UpahPengrajin)
        textHarga = findViewById(R.id.TextHarga)
        textJumlahEmas = findViewById(R.id.TextJumlahEmas)
        textTotal = findViewById(R.id.TextTotal)

        val hitungButton = findViewById<Button>(R.id.BtnHitung)
        hitungButton.setOnClickListener { calculateAndDisplayResult() }

        val resetButton = findViewById<Button>(R.id.BtnReset)
        resetButton.setOnClickListener {
            resetValues()
        }
    }

    private fun calculateAndDisplayResult() {
        try {
            val hargaEmas = hargaEmasEditText.text.toString().toDouble()
            val nilaiKarat = nilaiKaratEditText.text.toString().toDouble()
            val jumlahEmas = jumlahEmasEditText.text.toString().toDouble()
            val biayaPembuatan = upahPengrajinEditText.text.toString().toDouble()
            val hargaPerKarat = hargaEmas * nilaiKarat / 24

            val total = (hargaPerKarat * jumlahEmas) + (biayaPembuatan * jumlahEmas)

            textHarga.text = hargaPerKarat.toString()
            textJumlahEmas.text = jumlahEmas.toString()
            textTotal.text = total.toString()
        } catch (e: NumberFormatException) {
            textHarga.text = "Invalid input"
            textTotal.text = "Invalid input"
        }
    }

    private fun resetValues() {
        hargaEmasEditText.text.clear()
        nilaiKaratEditText.text.clear()
        jumlahEmasEditText.text.clear()
        upahPengrajinEditText.text.clear()
        textHarga.text = "Harga"
        textJumlahEmas.text = "Jumlah Emas"
        textTotal.text = "Total"
    }
}
