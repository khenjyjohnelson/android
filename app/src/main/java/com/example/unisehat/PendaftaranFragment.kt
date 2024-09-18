package com.example.unisehat
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unisehat.databinding.FragmentPendaftaranBinding
import com.example.unisehat.models.Daftar
import com.example.unisehat.models.Jadwal
import com.example.unisehat.models.mahasiswa
import com.google.firebase.database.*

class PendaftaranFragment : Fragment() {

    private var _binding: FragmentPendaftaranBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPendaftaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference



        binding.JadwalBtn.setOnClickListener {
            tambahJadwal()
        }

        binding.DaftarBtn.setOnClickListener {
            daftarUnisehat()
        }

        tampilkanInformasiPendaftaran()
    }

    private fun daftarUnisehat() {
        val dialogView = layoutInflater.inflate(R.layout.popup_pilih_jadwal, null)
        val recyclerViewJadwal = dialogView.findViewById<RecyclerView>(R.id.recyclerViewJadwal)
        val buttonDaftar = dialogView.findViewById<Button>(R.id.buttonDaftar)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Pilih Jadwal")
            .create()

        val jadwalList = mutableListOf<Jadwal>()
        val jadwalKeys = mutableListOf<String>()

        val adapter = JadwalAdapter(jadwalList)
        recyclerViewJadwal.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewJadwal.adapter = adapter

        database.child("jadwal").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                jadwalList.clear()
                jadwalKeys.clear()
                for (dataSnapshot in snapshot.children) {
                    val jadwal = dataSnapshot.getValue(Jadwal::class.java)
                    if (jadwal != null) {
                        jadwalList.add(jadwal)
                        jadwalKeys.add(dataSnapshot.key ?: "")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

        buttonDaftar.setOnClickListener {
            val selectedJadwalPosition = adapter.getSelectedPosition()
            if (selectedJadwalPosition != RecyclerView.NO_POSITION) {
                val jadwalId = jadwalKeys[selectedJadwalPosition]
                val sharedPref = activity?.let {
                    it.getSharedPreferences("userSession", Context.MODE_PRIVATE)
                }
                val userId = sharedPref?.getString("userId", null)

                val daftarId = database.child("daftar").push().key
                if (daftarId != null) {
                    val daftar = Daftar(userId, jadwalId)
                    database.child("daftar").child(daftarId).setValue(daftar)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Gagal mendaftar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Pilih jadwal terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }

    private fun tambahJadwal() {
        val dialogView = layoutInflater.inflate(R.layout.popup_add_jadwal, null)
        val editTextTanggal = dialogView.findViewById<EditText>(R.id.editTextTanggal)
        val editTextJam = dialogView.findViewById<EditText>(R.id.editTextJam)
        val editTextHari = dialogView.findViewById<EditText>(R.id.editTextHari)
        val buttonSimpan = dialogView.findViewById<Button>(R.id.buttonSimpan)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Tambah Jadwal Baru")
            .create()

        buttonSimpan.setOnClickListener {
            val tanggal = editTextTanggal.text.toString()
            val jam = editTextJam.text.toString()
            val hari = editTextHari.text.toString()

            if (tanggal.isNotEmpty() && jam.isNotEmpty() && hari.isNotEmpty()) {
                val jadwalId = database.child("jadwal").push().key
                if (jadwalId != null) {
                    val newJadwal = Jadwal(jadwalId, tanggal, jam, hari)
                    database.child("jadwal").child(jadwalId).setValue(newJadwal)
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan jadwal", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Harap lengkapi semua field", Toast.LENGTH_SHORT).show()
            }
        }
        alertDialog.show()
    }

    private fun tampilkanInformasiPendaftaran() {
        database.child("daftar").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tableLayout = binding.TablelayoutList
                tableLayout.removeAllViews() // Bersihkan semua baris sebelum menambahkan yang baru

                var no = 1
                for (daftarSnapshot in dataSnapshot.children) {
                    val daftar = daftarSnapshot.getValue(Daftar::class.java)
                    if (daftar != null) {
                        // Dapatkan informasi pengguna dari userId pada daftar
                        database.child("user").child(daftar.userId ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(mahasiswa::class.java)
                                if (user != null) {
                                    // Dapatkan informasi jadwal dari jadwalId pada daftar
                                    database.child("jadwal").child(daftar.jadwalId ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(jadwalSnapshot: DataSnapshot) {
                                            val jadwal = jadwalSnapshot.getValue(Jadwal::class.java)
                                            if (jadwal != null) {
                                                // Tambahkan informasi pendaftaran ke dalam tabel
                                                val row = TableRow(requireContext())
                                                val noTextView = TextView(requireContext())
                                                noTextView.text = no++.toString()
                                                val nameTextView = TextView(requireContext())
                                                nameTextView.text = user.nama // Sesuaikan dengan properti yang sesuai
                                                val jurusanTextView = TextView(requireContext())
                                                jurusanTextView.text = user.jurusan // Sesuaikan dengan properti yang sesuai
                                                val angkatanTextView = TextView(requireContext())
                                                angkatanTextView.text =
                                                    user.angkatan.toString() // Sesuaikan dengan properti yang sesuai
                                                val tanggalTextView = TextView(requireContext())
                                                tanggalTextView.text = jadwal.tanggal

                                                row.addView(noTextView)
                                                row.addView(nameTextView)
                                                row.addView(jurusanTextView)
                                                row.addView(angkatanTextView)
                                                row.addView(tanggalTextView)
                                                tableLayout.addView(row)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Error handling jika diperlukan
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Error handling jika diperlukan
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error handling jika diperlukan
            }
        })
    }

}
