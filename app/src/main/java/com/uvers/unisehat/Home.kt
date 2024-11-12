package com.uvers.unisehat
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.uvers.unisehat.control.getSavedCredentials
import com.uvers.unisehat.control.mapEnglishDayToIndonesian
import com.uvers.unisehat.models.AgendaItem
import com.uvers.unisehat.models.Jadwal
import com.uvers.unisehat.models.Users
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun Home() {
    var user by remember { mutableStateOf<Users?>(null) }
    var nearestAgenda by remember { mutableStateOf<AgendaItem?>(null) }
    val jadwalHariIni = remember { mutableStateListOf<Jadwal>() }

    val currentDate = Calendar.getInstance().time
    val currentDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(currentDate)

    var selectedDay by remember {
        mutableStateOf(
           mapEnglishDayToIndonesian(currentDay)
        )
    }

    var isLoading by remember { mutableStateOf(true) } // Indikasi loading


    val agendaList = remember { mutableStateListOf<AgendaItem>() }

    val context = LocalContext.current



    LaunchedEffect(Unit) {
        isLoading = true
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            val (identifier, role) = savedCredentials
            fetchUserData(identifier) {
                user = it
                val semester = user?.semester

                if (semester != null && semester.isNotBlank()) {
                    fetchJadwalList(semester) { jadwalList ->
                        jadwalHariIni.clear()
                        jadwalHariIni.addAll(
                            jadwalList.filter { jadwal -> jadwal.hari.equals(selectedDay, ignoreCase = true) }
                                .sortedBy { jadwal -> jadwal.hari }
                        )
                    }
                } else {
                    Log.e("Home", "Semester is null or blank.")
                    // Handle the case where semester is not available
                }
            }


        }

        fetchAgendaListHome { agendas ->
            agendaList.clear()
            agendaList.addAll(agendas)
        }
        isLoading = false // Set loading to false after data is loaded
    }

    val upcomingAgendas = agendaList.filter { it.date >= currentDate }.sortedBy { it.date }
    nearestAgenda = upcomingAgendas.firstOrNull()



    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val logoPainter: Painter = rememberImagePainter(
                            data = user?.photoUrl,
                            builder = {
                                placeholder(R.drawable.logotpl)
                                error(R.drawable.logotpl)
                            }
                        )
                        Image(
                            painter = logoPainter,
                            contentDescription = "Profile",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            user?.let {
                                Text(
                                    text = "Welcome, ${it.nama}!",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Text(
                                        text = if (it.role == "mahasiswa") "Nim" else "NIDN",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(text = " : ", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = it.identifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                if (it.role == "mahasiswa" && it.ketLulus == "Mahasiswa"){
                                    Row {
                                        Text(text = "Angkatan", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = " : ", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = it.angkatan,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row {
                                        Text(text = "Semester", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = " : ", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = it.semester,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if(user?.role == "mahasiswa"){

                    Text(
                        text = "Jadwal Hari ini",
                        modifier = Modifier.padding(top = 20.dp),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (jadwalHariIni.isNotEmpty()) {
                        jadwalHariIni.forEach { jadwal ->
                            OutlinedCard(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(13.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = jadwal.mataKuliah,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        Text(text = "Waktu", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = " : ", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(13.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Tidak ada jadwal hari ini",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                }




                Text(
                    text = "Agenda Terdekat",
                    modifier = Modifier.padding(top = 20.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        nearestAgenda?.let {
                            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val formattedDate = dateFormatter.format(it.date)
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date: ${formattedDate}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } ?: Text(
                            text = "Tidak ada agenda yang akan datang",
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }
                }

            }
        }

    }
}


private fun fetchUserData(identifier: String, onResult: (Users?) -> Unit) {
    val database = Firebase.database
    val usersRef = database.getReference("Users")
    usersRef.child(identifier).get().addOnSuccessListener { snapshot ->
        val user = snapshot.getValue(Users::class.java)
        onResult(user)
    }.addOnFailureListener {
        onResult(null)
    }
}

private fun fetchNearestAgenda(onResult: (String?) -> Unit) {
    val database = Firebase.database
    val agendaRef = database.getReference("agenda")
    // Get current date
    val currentDate = Calendar.getInstance().time



    agendaRef.orderByChild("date").startAt(currentDate.time.toDouble()).limitToFirst(1).get()
        .addOnSuccessListener { snapshot ->
            var nearestAgenda: String? = null
            if (snapshot.exists()) {
                // Assume that snapshot will return one or zero children
                val child = snapshot.children.firstOrNull()
                val agenda = child?.getValue(AgendaItem::class.java)
                nearestAgenda = agenda?.title
            }
            onResult(nearestAgenda)
        }.addOnFailureListener {
            onResult(null)
        }
}


fun fetchAgendaListHome(onComplete: (List<AgendaItem>) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("agenda")

    database.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val agendaList = mutableListOf<AgendaItem>()
            snapshot.children.forEach { childSnapshot ->
                val agenda = childSnapshot.getValue(AgendaItem::class.java)
                agenda?.let { agendaList.add(it) }
            }
            onComplete(agendaList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
}

private fun fetchJadwalList(semester: String, onComplete: (List<Jadwal>) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("jadwal")

    val query = database.orderByChild("semester").equalTo(semester)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val jadwalList = mutableListOf<Jadwal>()
            snapshot.children.forEach { childSnapshot ->
                val jadwal = childSnapshot.getValue(Jadwal::class.java)
                jadwal?.let { jadwalList.add(it) }
            }
            onComplete(jadwalList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error and return an empty list if there's an error
            onComplete(emptyList())
        }
    })
}
