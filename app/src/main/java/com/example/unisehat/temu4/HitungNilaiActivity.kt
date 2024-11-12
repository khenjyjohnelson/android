package com.example.unisehat.temu4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.unisehat.R

class HitungNilaiActivity : AppCompatActivity() {
    private lateinit var nilait:EditText
    private lateinit var nilaiuts:EditText
    private lateinit var nilaiuas:EditText
    private lateinit var namamhs:EditText
    private lateinit var hasilnama:EditText
    private lateinit var totalnilai:EditText
    private lateinit var nilaihuruf:EditText
    private lateinit var hitung:Button
    private lateinit var kembali:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hitung_nilai)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        namamhs = findViewById(R.id.editNamaMahasiswa)
        nilait = findViewById(R.id.editNilaiTugas)
        nilaiuts = findViewById(R.id.editNilaiUTS)
        nilaiuas = findViewById(R.id.editNilaiUAS)
        totalnilai = findViewById(R.id.editTotal)
        nilaihuruf = findViewById(R.id.editNilaiHuruf)
        hasilnama = findViewById(R.id.editNama)
        hitung = findViewById(R.id.dsn) // Initialize hitung button
        kembali = findViewById(R.id.mhs) // Initialize kembali button

        hitung.setOnClickListener {
            val nt = nilait.text.toString()
            val nuts = nilaiuts.text.toString()
            val uas = nilaiuas.text.toString()

            val nilaitotal = (nt.toFloat() + nuts.toFloat() + uas.toFloat()) / 3

            val grade = if(nilaitotal > 85) {
                'A'
            } else if (nilaitotal > 75){
                'B'
            } else {
                'C'
            }
            namamhs.setText("")
            hasilnama.setText(namamhs.toString())
            totalnilai.setText(nilaitotal.toString())
        }
    }


}