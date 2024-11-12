package com.example.unisehat

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var myDB: SQLiteDatabase? = null
    private val tableName = "Mahasiswa"
    private var data = ""
    private lateinit var dtMhs: TextView
    private lateinit var bSimpan: Button
    private lateinit var bEdit: Button
    private lateinit var bHapus: Button
    private lateinit var tNim: EditText
    private lateinit var tNama: EditText
    private lateinit var tAlamat: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dtMhs = findViewById(R.id.txtDataMhs)
        bSimpan = findViewById(R.id.btnSimpan)
        bEdit = findViewById(R.id.btnEdit)
        bHapus = findViewById(R.id.btnHapus)
        tNim = findViewById(R.id.txtNim)
        tNama = findViewById(R.id.txtNama)
        tAlamat = findViewById(R.id.txtAlamat)

        createDB()
        tampilData()

        bSimpan.setOnClickListener { simpan() }
        bEdit.setOnClickListener { edit() }
        bHapus.setOnClickListener { hapus() }
    }

    private fun clearField() {
        tNim.text.clear()
        tNama.text.clear()
        tAlamat.text.clear()
    }

    private fun createDB() {
        try {
            myDB = this.openOrCreateDatabase("DBMHS", MODE_PRIVATE, null)
            myDB?.execSQL("CREATE TABLE IF NOT EXISTS $tableName(NIM VARCHAR PRIMARY KEY, NAMA VARCHAR, ALAMAT VARCHAR);")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tampilData() {
        try {
            data = ""
            clearField()
            myDB?.rawQuery("SELECT * FROM $tableName", null)?.use { c ->
                val col1 = c.getColumnIndex("NIM")
                val col2 = c.getColumnIndex("NAMA")
                val col3 = c.getColumnIndex("ALAMAT")
                while (c.moveToNext()) {
                    val nimMhs = c.getString(col1)
                    val nmMhs = c.getString(col2)
                    val almtMhs = c.getString(col3)
                    data += "$nimMhs | $nmMhs | $almtMhs\n"
                }
            }
            dtMhs.text = data
        } catch (e: Exception) {
            e.printStackTrace()
            dtMhs.text = data
        }
    }

    private fun simpan() {
        try {
            myDB?.execSQL("INSERT INTO $tableName VALUES('${tNim.text}', '${tNama.text}', '${tAlamat.text}');")
            tampilData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun edit() {
        try {
            myDB?.execSQL("UPDATE $tableName SET NAMA = '${tNama.text}', ALAMAT = '${tAlamat.text}' WHERE NIM = '${tNim.text}';")
            tampilData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hapus() {
        try {
            myDB?.execSQL("DELETE FROM $tableName WHERE NIM = '${tNim.text}';")
            tampilData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
