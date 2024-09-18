package com.example.unisehat

import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import com.example.unisehat.databinding.FragmentDashboardBinding
import com.example.unisehat.databinding.PopupAddUserBinding
import com.example.unisehat.models.mahasiswa
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import kotlin.math.log

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference
        Log.d("DashboardFragment", "database: $database")
        binding.addUserBtn.setOnClickListener {
            showAddMahasiswaPopup()
        }

        readData()
    }


    private fun readData() {
        Log.d("DashboardFragment", "read data called")
        database.child("mahasiswa").get().addOnSuccessListener { dataSnapshot ->
            val mahasiswas = mutableListOf<String>()
            binding.tableLayout.removeAllViews()

            // Add a header row
            val headerRow = TableRow(context)
            headerRow.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val noHeaderTextView = TextView(context)
            noHeaderTextView.text = "No"
            noHeaderTextView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            noHeaderTextView.gravity = Gravity.CENTER
            headerRow.addView(noHeaderTextView)

            val nameHeaderTextView = TextView(context)
            nameHeaderTextView.text = "Name"
            nameHeaderTextView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            nameHeaderTextView.gravity = Gravity.LEFT
            headerRow.addView(nameHeaderTextView)

            val detailHeaderTextView = TextView(context)
            detailHeaderTextView.text = "Detail"
            detailHeaderTextView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            detailHeaderTextView.gravity = Gravity.CENTER
            headerRow.addView(detailHeaderTextView)

            val statusHeaderTextView = TextView(context)
            statusHeaderTextView.text = "Status"
            statusHeaderTextView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            statusHeaderTextView.gravity = Gravity.CENTER
            headerRow.addView(statusHeaderTextView)

            val actionHeaderTextView = TextView(context)
            actionHeaderTextView.text = "Action"
            actionHeaderTextView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            actionHeaderTextView.gravity = Gravity.CENTER
            headerRow.addView(actionHeaderTextView)

            binding.tableLayout.addView(headerRow)

            // Add rows for each student
            if (dataSnapshot.exists()) {
                for (data in dataSnapshot.children) {
                    val mahasi = data.getValue(mahasiswa::class.java)
                    mahasi?.let {
                        // Create a new TableRow for each student
                        val tableRow = TableRow(context)

                        // Add TextViews for each property of the student object
                        val noTextView = TextView(context)
                        if (binding.tableLayout.childCount > 0) {
                            noTextView.text = (binding.tableLayout.childCount).toString()
                        }
                        noTextView.gravity = Gravity.CENTER
                        tableRow.addView(noTextView)

                        val nameTextView = TextView(context)
                        nameTextView.text = mahasi.nama
                        nameTextView.gravity = Gravity.LEFT
                        tableRow.addView(nameTextView)

                        val detailTextView = TextView(context)
                        detailTextView.text = "detail"
                        detailTextView.gravity = Gravity.CENTER
                        tableRow.addView(detailTextView)

                        val statusTextView = TextView(context)
                        statusTextView.text = "status"
                        tableRow.addView(statusTextView)

                        val actionTextView = TextView(context)
                        actionTextView.text = "Action"
                        actionTextView.gravity = Gravity.CENTER
                        tableRow.addView(actionTextView)

                        // Add the TableRow to the TableLayout
                        binding.tableLayout.addView(tableRow)
                    }
                }
            } else {
                Toast.makeText(context, "No mahasiswa data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.d("DashboardFragment", "Error: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showAddMahasiswaPopup() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_add_user, null)
        val dialogBuilder = AlertDialog.Builder(requireActivity())
            .setView(dialogView)

        val editNim = dialogView.findViewById<EditText>(R.id.editNim)
        val editNama = dialogView.findViewById<EditText>(R.id.editNama)
        val editEmail = dialogView.findViewById<EditText>(R.id.editEmail)
        val editJurusan = dialogView.findViewById<EditText>(R.id.editJurusan)
        val editAngkatan = dialogView.findViewById<EditText>(R.id.editAngkatan)
        val editPassword = dialogView.findViewById<EditText>(R.id.editpass)
        val buttonSave = dialogView.findViewById<Button>(R.id.button_save)

        dialogBuilder.setTitle("Add Mahasiswa")

        val dialog = dialogBuilder.create()

        buttonSave.setOnClickListener {
            val nim = editNim.text.toString()
            val nama = editNama.text.toString()
            val email = editEmail.text.toString()
            val jurusan = editJurusan.text.toString()
            val angkatan = editAngkatan.text.toString().toInt()
            val password = editPassword.text.toString()
            Log.d("DashboardFragment", "nim: $nim, nama: $nama, email: $email, jurusan: $jurusan, angkatan: $angkatan, password: $password")

            val hashedPassword = hashPassword(password)

            Log.d("DashboardFragment", "hashedPassword: $hashedPassword")
            // Memanggil fungsi untuk menyimpan data ke Firebase Realtime Database
            saveMahasiswaToDatabase(nim, nama, email, jurusan, angkatan, hashedPassword)
            Log.d("DashboardFragment", "saveMahasiswaToDatabase called")

            dialog.dismiss()
            Log.d("DashboardFragment", "dialog dismissed")
        }

        dialog.show()
        Log.d("DashboardFragment", "dialog shown")
    }

    private fun validateMahasiswaData(binding: PopupAddUserBinding): String {
        val errorMessage = StringBuilder()
        if(binding.editNim.text.isNullOrEmpty()){
            errorMessage.append("NIM is required.\n")
        }
        if (binding.editNama.text.isNullOrEmpty()){
            errorMessage.append("Nama is required.\n")
        }
        if(binding.editpass.text.isNullOrEmpty()){
            errorMessage.append("Password is required")
        }
        return errorMessage.toString().trim()
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

    private fun saveMahasiswaToDatabase(nim: String, nama: String, email: String, jurusan: String, angkatan: Int, hashedPassword: String ) {
        Log.d("DashboardFragment","save mahasiswa called ============")
        val userId = database.child("mahasiswa").push().key
        val mahas = mahasiswa(
            userId, nim, nama, email, jurusan, angkatan, hashedPassword
        )
        Log.d("DashboardFragment", "mahas: $mahas")
        userId?.let{
            Log.d("DashboardFragment", "userId: $userId")

            database.child("mahasiswa").child(it).setValue(mahas)
                .addOnSuccessListener {
                    Log.d("DashboardFragment", "Database saved")
                    Toast.makeText(context, "Data Stored Succesfully", Toast.LENGTH_SHORT).show()
                    readData()
                }
                .addOnFailureListener{ e ->
                    Log.d("DashboardFragment", "Error: ${e.message}")
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan binding saat fragment dihancurkan
        _binding = null
    }

}
