package com.example.unisehat.lainnya

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.unisehat.R
import java.util.Arrays

class MahasiswaActivity : AppCompatActivity() {
    // Data yang Akan dimasukan Pada ListView
    private val mahasiswa = arrayOf(
        "Wildan",
        "Taufan",
        "Adibil",
        "Hari",
        "Adam",
        "Hermawan",
        "Indra",
        "Widi",
        "Anisa",
        "Hani"
    )

    // ArrayList digunakan Untuk menampung Data mahasiswa
    private var data: ArrayList<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mahasiswa)
        val listView = findViewById<ListView>(R.id.list)
        data = ArrayList()
        getData()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, data!!
        )
        listView.setAdapter(adapter)
    }

    private fun getData() {
        // Memasukan Semua Data mahasiswa kedalam ArrayList
        data!!.addAll(Arrays.asList(*mahasiswa))
    }
}
