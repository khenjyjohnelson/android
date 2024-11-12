package com.uvers.TplApplication

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uvers.TplApplication.control.LoadingScreen
import com.uvers.TplApplication.control.getSavedCredentials
import com.uvers.TplApplication.control.mapEnglishDayToIndonesian
import com.uvers.TplApplication.models.Jadwal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@Composable
fun Jadwal(){
    val JadwalNavController = rememberNavController()
    NavHost(navController = JadwalNavController, startDestination = "jadwalList") {
        composable("jadwalList") {
            JadwalScreen(JadwalNavController)
        }
        composable("addJadwal") {
            AddJadwalScreen(JadwalNavController)
        }
        composable("editJadwal/{jadwalId}",
            arguments = listOf(navArgument("jadwalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jadwalId = backStackEntry.arguments?.getString("jadwalId")
            if(jadwalId != null){
                var jadwal by remember { mutableStateOf<Jadwal?>(null) }
                LaunchedEffect(jadwalId) {
                    fetchJadwalFromFirebase(jadwalId) { fetchjadwal ->
                       jadwal = fetchjadwal
                    }
                }

                jadwal?.let{ jadwal ->
                    EditJadwalScreen(
                        jadwal = jadwal,
                        onSubmit = { updateJadwal ->
                            updateJadwalInFirebase(updateJadwal, onComplete = {
                                JadwalNavController.popBackStack()
                            })
                        },
                        onCancel = {
                            JadwalNavController.popBackStack()
                        }
                        )

                }
            }
        }
        composable("detailJadwal/{jadwalId}") { backStackEntry ->
            val jadwalId = backStackEntry.arguments?.getString("jadwalId") ?: ""
            DetailJadwalScreen(JadwalNavController, jadwalId)
        }
    }
}

@Composable
fun JadwalScreen(navController: NavController) {
    val context = LocalContext.current

    var jadwalList by remember { mutableStateOf(listOf<Jadwal>()) }
    var selectedJadwal by remember { mutableStateOf<Jadwal?>(null) }
    val scope = rememberCoroutineScope()
    var showDialogDeleteJadwal by remember { mutableStateOf(false) }

    val (identifier, role) = getSavedCredentials(context) ?: return

    var selectedSemester by remember { mutableStateOf("Semester 1") }

    val currentDate = Calendar.getInstance().time
    val currentDayEnglish = SimpleDateFormat("EEEE", Locale.ENGLISH).format(currentDate)
    var selectedDay by remember {
        mutableStateOf(
            if (isWeekend()) "All" else mapEnglishDayToIndonesian(currentDayEnglish)
        )
    }

    var semesterExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    val semesterList = listOf(
        "Semester 1", "Semester 2", "Semester 3", "Semester 4",
        "Semester 5", "Semester 6", "Semester 7", "Semester 8"
    )
    val dayList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")

    // Custom order for sorting days
    val dayOrder = mapOf(
        "Senin" to 1,
        "Selasa" to 2,
        "Rabu" to 3,
        "Kamis" to 4,
        "Jumat" to 5
    )

    LaunchedEffect(Unit) {
        fetchJadwalListFromFirebase { fetchedJadwalList ->
            jadwalList = fetchedJadwalList
        }

        getUserSemester(context) { userSemester ->
            if (userSemester != null && userSemester in semesterList) {
                selectedSemester = userSemester
            } else {
                selectedSemester = "Semester 1"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = selectedSemester,
                    onValueChange = { selectedSemester = it },
                    label = { Text("Semester") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { semesterExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { semesterExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pick Day"
                            )
                        }
                    })
                DropdownMenu(
                    expanded = semesterExpanded,
                    onDismissRequest = { semesterExpanded = false }
                ) {
                    semesterList.forEach { semester ->
                        DropdownMenuItem(
                            text = { Text(text = semester) },
                            onClick = {
                                selectedSemester = semester
                                semesterExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = selectedDay,
                    onValueChange = { selectedDay = it },
                    label = { Text("Hari") },
                    readOnly = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { dayExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { dayExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pick Day"
                            )
                        }
                    })
                DropdownMenu(
                    expanded = dayExpanded,
                    onDismissRequest = { dayExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "All") },
                        onClick = {
                            selectedDay = "All"
                            dayExpanded = false
                        }
                    )
                    dayList.forEach { day ->
                        DropdownMenuItem(
                            text = { Text(text = day) },
                            onClick = {
                                selectedDay = day
                                dayExpanded = false
                            }
                        )
                    }
                }
            }
        }



        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (role == "admin") {
            item {
                Row( modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    Button(onClick = { navController.navigate("addJadwal") }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Jadwal")
                    }
                }

            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }


        val filteredJadwalList = jadwalList.filter {
            it.semester == selectedSemester && (selectedDay == "All" || it.hari.equals(selectedDay, ignoreCase = true))
        }.sortedBy { dayOrder[it.hari] ?: Int.MAX_VALUE }

        if (filteredJadwalList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = "Tidak ada Jadwal",
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredJadwalList) { jadwal ->
                JadwalCard(
                    jadwal = jadwal,
                    onDetailClick = { navController.navigate("detailJadwal/${jadwal.number}") },
                    onEditClick = { navController.navigate("editJadwal/${jadwal.number}") },
                    onDeleteClick = {
                        selectedJadwal = jadwal
                        showDialogDeleteJadwal = true
                    }
                )
            }
        }

    }

    if (showDialogDeleteJadwal) {
        AlertDialog(
            onDismissRequest = { showDialogDeleteJadwal = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${selectedJadwal?.mataKuliah}?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            selectedJadwal?.let {
                                jadwalList =
                                    jadwalList.filter { it.number != selectedJadwal?.number }
                                deleteJadwalFromFirebase(selectedJadwal!!.number.toString()) { success ->
                                    if (success) {
                                        fetchJadwalListFromFirebase { fetchedJadwalList ->
                                            jadwalList = fetchedJadwalList
                                        }
                                    }
                                }
                            }
                            showDialogDeleteJadwal = false
                            selectedJadwal = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialogDeleteJadwal = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun isWeekend(): Boolean {
    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    return currentDayOfWeek == Calendar.SATURDAY || currentDayOfWeek == Calendar.SUNDAY
}

@Composable
fun JadwalCard(jadwal: Jadwal, onDetailClick: (Jadwal) -> Unit, onEditClick: (Jadwal) -> Unit, onDeleteClick: () -> Unit) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = { onDetailClick(jadwal) }),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = jadwal.mataKuliah, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
            Text(text = "${jadwal.hari}, ${jadwal.waktuMulai} - ${jadwal.waktuSelesai}", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = { onDetailClick(jadwal) }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Detail")
                }
                if(role == "admin" ){
                    IconButton(onClick = { onEditClick(jadwal) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }

                    IconButton(onClick = { onDeleteClick() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete",  tint = Color.Red)
                    }
                }

            }
        }
    }
}


@Composable
fun AddJadwalScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) } // Add this state

    var mataKuliah by remember { mutableStateOf("") }
    var hari by remember { mutableStateOf("") }
    var waktuMulai by remember { mutableStateOf("") }
    var waktuSelesai by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf("") }
    var mahasiswa by remember { mutableStateOf("") }
    var kodeMk by remember { mutableStateOf("") }
    var ruangan by remember { mutableStateOf("") }
    var dosen by remember { mutableStateOf("") }

    var expandedsemester by remember { mutableStateOf(false) }
    var expandedday by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val dayList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
    val semesterList = listOf("Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5","Semester 6","Semester 7","Semester 8")

    var mkIsEmpty by remember { mutableStateOf(false) }
    var wmIsEmpty by remember { mutableStateOf(false) }
    var wsIsEmpty by remember { mutableStateOf(false) }
    var semesterIsEmpty by remember { mutableStateOf(false) }
    var hariIsEmpty by remember { mutableStateOf(false) }


    val context = LocalContext.current

    fun openTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formattedTime)
            },
            hour, minute, true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = kodeMk,
            onValueChange = { kodeMk = it },
            label = { Text("Kode MK") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = mataKuliah,
            onValueChange = { mataKuliah = it },
            label = { Text("Mata Kuliah") },
            isError = mkIsEmpty,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = hari,
            onValueChange = { hari = it },
            label = { Text("Hari") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),

            isError = hariIsEmpty,
            trailingIcon = {
                IconButton(onClick = { expandedday = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Day")
                }
            }
        )
        DropdownMenu(
            expanded = expandedday,
            onDismissRequest = { expandedday = false }
        ) {
            dayList.forEach { day ->
                DropdownMenuItem(
                    text = { Text(text = day) },
                    onClick = {
                        hari = day
                        expandedday = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = ruangan,
            onValueChange = { ruangan = it },
            label = { Text("Ruangan") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = waktuMulai,
            onValueChange = { waktuMulai = it },
            label = { Text("Waktu Mulai") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            isError = wmIsEmpty,
            trailingIcon = {
                IconButton(onClick = { openTimePickerDialog { waktuMulai = it } }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Time")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = waktuSelesai,
            onValueChange = { waktuSelesai = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Waktu Selesai") },
            readOnly = true,
            isError = wsIsEmpty,

            trailingIcon = {
                IconButton(onClick = { openTimePickerDialog { waktuSelesai = it } }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Time")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = semester,
            onValueChange = { semester = it },
            label = { Text("Semester") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = semesterIsEmpty,

            trailingIcon = {
                IconButton(onClick = { expandedsemester = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick semester")
                }
            }
        )
        DropdownMenu(
            expanded = expandedsemester,
            onDismissRequest = { expandedsemester = false }
        ) {
            semesterList.forEach { semestera ->
                DropdownMenuItem(
                    text = { Text(text = semestera) },
                    onClick = {
                        semester = semestera
                        expandedsemester = false
                    }
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Row{
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                mkIsEmpty = mataKuliah.isEmpty()
                hariIsEmpty = hari.isEmpty()
                wsIsEmpty = waktuSelesai.isEmpty()
                wmIsEmpty = waktuMulai.isEmpty()
                semesterIsEmpty = semester.isEmpty()

                if(semester.isEmpty() || waktuSelesai.isEmpty() || waktuMulai.isEmpty() || hari.isEmpty() || mataKuliah.isEmpty()){
                    return@Button
                }

                isLoading = true

                val jadwal = Jadwal(
                    number = UUID.randomUUID().toString(), // Generate UUID untuk id // Temporary ID, will be replaced by the incremented value
                    mataKuliah = mataKuliah,
                    hari = hari,
                    waktuMulai = waktuMulai,
                    waktuSelesai = waktuSelesai,
                    semester = semester
                )
                scope.launch{
                    saveJadwalToFirebase(jadwal) { success ->
                        if (success) {
                            isLoading = false
                            navController.popBackStack()
                        }
                    }
                }

            }) {
                Text("Add Jadwal")
            }


        }

    }
    LoadingScreen(isLoading)
}



@Composable
fun EditJadwalScreen(jadwal: Jadwal, onSubmit: (Jadwal) -> Unit, onCancel: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) } // Add this state

    var mataKuliah by remember { mutableStateOf(jadwal.mataKuliah) }
    var hari by remember { mutableStateOf(jadwal.hari) }
    var waktuMulai by remember { mutableStateOf(jadwal.waktuMulai) }
    var waktuSelesai by remember { mutableStateOf(jadwal.waktuSelesai) }
    var semester by remember { mutableStateOf(jadwal.semester) }
    val scope = rememberCoroutineScope()
    var expandedsemester by remember { mutableStateOf(false) }
    var expandedday by remember { mutableStateOf(false) }
    val dayList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
    val semesterList = listOf("Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5","Semester 6","Semester 7","Semester 8")

    var mkIsEmpty by remember { mutableStateOf(false) }
    var wmIsEmpty by remember { mutableStateOf(false) }
    var wsIsEmpty by remember { mutableStateOf(false) }
    var semesterIsEmpty by remember { mutableStateOf(false) }
    var hariIsEmpty by remember { mutableStateOf(false) }


    val context = LocalContext.current

    fun openTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formattedTime)
            },
            hour, minute, true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = mataKuliah,
            onValueChange = { mataKuliah = it },
            label = { Text("Mata Kuliah") },
            isError = mkIsEmpty,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = hari,
            onValueChange = { hari = it },
            label = { Text("Hari") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = hariIsEmpty,

            trailingIcon = {
                IconButton(onClick = {expandedday = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Day")
                }
            }
        )
        DropdownMenu(
            expanded = expandedday,
            onDismissRequest = { expandedday = false }
        ) {
            dayList.forEach { day ->
                DropdownMenuItem(
                    text = { Text(text = day) },
                    onClick = {
                        hari = day
                        expandedday = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = waktuMulai,
            onValueChange = { waktuMulai = it },
            label = { Text("Waktu Mulai") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = wmIsEmpty,

            trailingIcon = {
                IconButton(onClick = { openTimePickerDialog { waktuMulai = it } }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Time")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = waktuSelesai,
            onValueChange = { waktuSelesai = it },
            label = { Text("Waktu Selesai") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = wsIsEmpty,

            trailingIcon = {
                IconButton(onClick = { openTimePickerDialog { waktuSelesai = it } }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Time")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = semester,
            onValueChange = { semester = it },
            label = { Text("Semester") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            isError = semesterIsEmpty,

            trailingIcon = {
                IconButton(onClick = {expandedsemester = true}) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Pick Semester")
                }
            }
        )
        DropdownMenu(
            expanded = expandedsemester,
            onDismissRequest = { expandedsemester = false }
        ) {
            semesterList.forEach { semestera ->
                DropdownMenuItem(
                    text = { Text(text = semestera) },
                    onClick = {
                        semester = semestera
                        expandedsemester = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row{
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                mkIsEmpty = mataKuliah.isEmpty()
                hariIsEmpty = hari.isEmpty()
                wsIsEmpty = waktuSelesai.isEmpty()
                wmIsEmpty = waktuMulai.isEmpty()
                semesterIsEmpty = semester.isEmpty()

                if(semester.isEmpty() || waktuSelesai.isEmpty() || waktuMulai.isEmpty() || hari.isEmpty() || mataKuliah.isEmpty()){
                    return@Button
                }

                isLoading = true
                val updatedJadwal = Jadwal(
                    number = jadwal.number,
                    mataKuliah = mataKuliah,
                    hari = hari,
                    waktuMulai = waktuMulai,
                    waktuSelesai = waktuSelesai,
                    semester = semester
                )
                scope.launch {
                    onSubmit(updatedJadwal)
                }
                isLoading = false
            }) {
                Text("Update Jadwal")
            }
        }

    }

    LoadingScreen(isLoading)
}

@Composable
fun DetailJadwalScreen(navController: NavController, jadwalId: String) {
    var jadwal by remember { mutableStateOf<Jadwal?>(null) }

    LaunchedEffect(jadwalId) {
        fetchJadwalFromFirebase(jadwalId) {
            jadwal = it
        }
    }

    val colors = MaterialTheme.colorScheme


    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        jadwal?.let {currentJadwal ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ){
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = currentJadwal.mataKuliah,
                            fontSize = 24.sp,
                            color = colors.primary,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = "${currentJadwal.hari}, ${currentJadwal.waktuMulai} - ${currentJadwal.waktuSelesai}",
                            fontSize = 16.sp,
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(text = "Semester: ${currentJadwal.semester}",
                            fontSize = 16.sp,
                            color = colors.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = colors.onSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant)
                            ) {
                                Text("Back", color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }?: run {
            // Show a loading indicator or placeholder if the agenda is not yet loaded
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        }

    }
}

fun fetchJadwalListFromFirebase(onComplete: (List<Jadwal>) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("jadwal")

    database.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val jadwalList = mutableListOf<Jadwal>()
            for (data in snapshot.children) {
                val jadwal = data.getValue(Jadwal::class.java)
                if (jadwal != null) {
                    jadwalList.add(jadwal)
                }
            }
            onComplete(jadwalList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
}



fun fetchJadwalFromFirebase(jadwalId: String, onComplete: (Jadwal?) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("jadwal").child(jadwalId)

    database.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val jadwal = snapshot.getValue(Jadwal::class.java)
            onComplete(jadwal)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle error
        }
    })
}

fun saveJadwalToFirebase(jadwal: Jadwal, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val jadwalRef = database.getReference("jadwal").child(jadwal.number.toString())

    jadwalRef.setValue(jadwal).addOnCompleteListener { task ->
        onComplete(task.isSuccessful)
    }

}

fun updateJadwalInFirebase(jadwal: Jadwal, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val jadwalref = database.getReference("jadwal").child(jadwal.number.toString())

    jadwalref.setValue(jadwal)
        .addOnCompleteListener {task ->
            onComplete(task.isSuccessful)
        }
        .addOnFailureListener {
            // Handle failure
        }
}

fun deleteJadwalFromFirebase(jadwalId: String, onComplete: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference.child("jadwal").child(jadwalId)

    database.removeValue()
        .addOnSuccessListener {
            // Success
        }
        .addOnFailureListener {
            // Handle failure
        }.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
}

fun getUserSemester(context: Context, callback: (String?) -> Unit) {
    val savedCredentials = getSavedCredentials(context)
    if (savedCredentials != null) {
        val (identifier, _) = savedCredentials
        val database = FirebaseDatabase.getInstance().reference
        database.child("Users").child(identifier).get().addOnSuccessListener { dataSnapshot ->
            val userSemester = dataSnapshot.child("semester").value?.toString()
            callback(userSemester)
        }.addOnFailureListener {
            callback(null)
        }
    } else {
        callback(null)
    }
}
