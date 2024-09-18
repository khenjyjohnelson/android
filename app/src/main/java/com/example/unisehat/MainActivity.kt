package com.example.unisehat
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unisehat.HomeActivity
import com.example.unisehat.R
import com.example.unisehat.models.mahasiswa
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference

        val loginButton = findViewById<Button>(R.id.buttonlogin)
        val nimEditText = findViewById<EditText>(R.id.editNim)
        val passwordEditText = findViewById<EditText>(R.id.editPassword)

        loginButton.setOnClickListener {
            val nim = nimEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Cek apakah NIM dan password sudah diisi
            if (nim.isNotEmpty() && password.isNotEmpty()) {
                val hashPw = hashPassword(password)
                loginUser(nim, hashPw)
            } else {
                // Jika NIM atau password kosong, tampilkan pesan kesalahan
                // Anda dapat menyesuaikan pesan kesalahan sesuai dengan kebutuhan Anda
                Toast.makeText(this, "Harap lengkapi NIM dan password", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(password.toByteArray())
        val hashedBytes = messageDigest.digest()
        val hashedString = StringBuilder()
        for (byte in hashedBytes) {
            hashedString.append("%02x".format(byte.toInt() and 0xff))
        }
        return hashedString.toString()
    }

    private fun loginUser(nim: String, password: String) {
        // Dapatkan referensi ke daftar pengguna di Firebase Realtime Database
        val usersRef = database.child("mahasiswa")

        // Cari pengguna berdasarkan NIM
        usersRef.orderByChild("nim").equalTo(nim).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Pengguna dengan NIM yang cocok ditemukan
                    for (userSnapshot in dataSnapshot.children) {
                        val dbpass = userSnapshot.child("password").getValue(String::class.java)
                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        if (dbpass == password) {
                            // Simpan data pengguna ke SharedPreferences
                            val sharedPref = getSharedPreferences("userSession", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("userId", userId)
                                putString("userNim", nim)
                                apply() // Simpan perubahan
                            }

                            // Beralih ke HomeActivity
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                            return
                        } else {
                            Toast.makeText(this@MainActivity, "Password salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Pengguna dengan NIM yang diberikan tidak ditemukan
                    Toast.makeText(this@MainActivity, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error handling jika ada kesalahan saat mengambil data dari database
                Toast.makeText(this@MainActivity, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
