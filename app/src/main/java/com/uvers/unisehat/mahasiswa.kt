package com.uvers.unisehat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uvers.unisehat.control.LoadingScreen
import com.uvers.unisehat.control.UpdatePasswordDialog
import com.uvers.unisehat.control.addUser
import com.uvers.unisehat.control.deleteUser
import com.uvers.unisehat.control.fetchUser
import com.uvers.unisehat.control.getSavedCredentials
import com.uvers.unisehat.control.updatePassword
import com.uvers.unisehat.control.updateUser
import com.uvers.unisehat.models.Users
import kotlinx.coroutines.launch
import java.util.Calendar


@Composable
fun MahasiswaMain() {
    val MahasiswaNavController = rememberNavController()

    NavHost(MahasiswaNavController, startDestination = "mahasiswaList") {
        composable("mahasiswaList") {
            MahasiswaScreen(MahasiswaNavController)
        }
        composable("addMahasiswa") {
            AddMahasiswaForm(
                onSubmit = { user, imageUri ->
                    addUser(user, imageUri, onComplete = {
                        MahasiswaNavController.popBackStack()
                    })
                },
                onCancel = {
                    MahasiswaNavController.popBackStack()
                }
            )
        }
        composable("editMahasiswa/{identifier}",
            arguments = listOf(navArgument("identifier") { type = NavType.StringType })) { backStackEntry ->
            val mahasiswaId = backStackEntry.arguments?.getString("identifier")
            // Fetch mahasiswa from your list or database using mahasiswaId
            // val mahasiswa = ...
            if (mahasiswaId != null) {
                var mahasiswa by remember { mutableStateOf<Users?>(null) }

                LaunchedEffect(mahasiswaId) {
                    fetchUser(mahasiswaId) { fetchedMahasiswa ->
                        mahasiswa = fetchedMahasiswa
                    }
                }

                mahasiswa?.let { mahasiswa ->
                    EditMahasiswaForm(
                        mahasiswa = mahasiswa,
                        onSubmit = { updatedMahasiswa, imageUri ->
                            updateUser(updatedMahasiswa, imageUri, onComplete = {
                                MahasiswaNavController.popBackStack()
                            })
                        },
                        onCancel = {
                            MahasiswaNavController.popBackStack()
                        }
                    )
                }
            }
        }

        composable(
            "detailMahasiswa/{identifier}",
            arguments = listOf(navArgument("identifier") { type = NavType.StringType })

        ) { backStackEntry ->
            val mahasiswaId = backStackEntry.arguments?.getString("identifier")
            if(mahasiswaId != null){
                var mahasiswa by remember { mutableStateOf<Users?>(null) }

                LaunchedEffect(mahasiswaId) {
                    fetchUser(mahasiswaId) { fetchdosen ->
                        mahasiswa = fetchdosen
                    }
                }

                mahasiswa?.let{mahasiswa ->
                    DetailMahasiswaDialog(MahasiswaNavController, mahasiswa = mahasiswa, onCancel = {
                        MahasiswaNavController.popBackStack()
                    })
                }
            }
        }

    }
}




@Composable
fun MahasiswaScreen(navController: NavController) {
    var selectedAngkatan by remember { mutableStateOf("All") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val angkatanList = mutableListOf("All")
    for (year in 2016..currentYear) {
        angkatanList.add(year.toString())
    }

    var expanded by remember { mutableStateOf(false) }

    var selectedMahasiswa by remember { mutableStateOf<Users?>(null) }
    val scope = rememberCoroutineScope()

    // Firebase Database reference
    val database = FirebaseDatabase.getInstance()
    val mahasiswaRef = database.getReference("Users")

    // State for holding Mahasiswa list
    val mahasiswaList by remember { mutableStateOf(mutableStateListOf<Users>()) }

    // Fetch Mahasiswa data from Firebase
    LaunchedEffect(Unit) {
        fetchMahasiswaFromFirebase(mahasiswaRef, mahasiswaList)
    }

    val filteredAndGroupedMahasiswa = mahasiswaList
        .filter { selectedAngkatan == "All" || it.angkatan == selectedAngkatan }
        .sortedBy { it.angkatan }  // Sort by identifier
        .groupBy { it.angkatan }  // Group by angkatan

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filter UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                OutlinedTextField(
                    value = selectedAngkatan,
                    onValueChange = { selectedAngkatan = it },
                    label = { Text("Angkatan") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    angkatanList.forEach { angkatan ->
                        DropdownMenuItem(
                            text = { Text(text = angkatan) },
                            onClick = {
                                selectedAngkatan = angkatan
                                expanded = false
                            })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mahasiswa list
        MahasiswaList(
            filteredAndGroupedMahasiswa = filteredAndGroupedMahasiswa,
            onAddClick = {
                navController.navigate("addMahasiswa")
            },
            onDetailClick = { mahasiswa ->
                navController.navigate("detailMahasiswa/${mahasiswa.identifier}")
            },
            onEditClick = { mahasiswa ->
                navController.navigate("editMahasiswa/${mahasiswa.identifier}")
            },
            onDeleteClick = { mahasiswa ->
                selectedMahasiswa = mahasiswa
                showDeleteDialog = true
            }
        )

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete ${selectedMahasiswa?.nama}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                selectedMahasiswa?.let {
                                    mahasiswaList.remove(it)
                                    deleteUser(it.identifier) { success ->
                                        if (success) {
                                            fetchMahasiswaFromFirebase(mahasiswaRef, mahasiswaList)
                                        }
                                    }
                                }
                                showDeleteDialog = false
                                selectedMahasiswa = null
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}

@Composable
fun MahasiswaList(
    filteredAndGroupedMahasiswa: Map<String, List<Users>>,
    onAddClick: () -> Unit,
    onDetailClick: (Users) -> Unit,
    onEditClick: (Users) -> Unit,
    onDeleteClick: (Users) -> Unit,
) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add button at the bottom if the user is an admin
        if (role == "admin") {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAddClick,
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Mahasiswa")
                    }
                }
            }
        }

        // Display grouped data by angkatan
        filteredAndGroupedMahasiswa.forEach { (angkatan, mahasiswaGroup) ->
            item {
                Text(
                    text = "Angkatan $angkatan",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                // Ensure each group is sorted by identifier
                val sortedMahasiswaGroup = mahasiswaGroup.sortedBy { it.identifier }

                Grid(items = sortedMahasiswaGroup) { mahasiswa ->
                    MahasiswaCard(
                        mahasiswa = mahasiswa,
                        onDetailClick = { onDetailClick(mahasiswa) },
                        onEditClick = { onEditClick(mahasiswa) },
                        onDeleteClick = { onDeleteClick(mahasiswa) }
                    )
                }
            }
        }


    }
}
@Composable
fun DetailMahasiswaDialog(
    navController: NavController,
    mahasiswa: Users?,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    var showUpdatePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    // Check if mahasiswa is not null
    mahasiswa?.let {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Center the content horizontally
            ) {
                // Display mahasiswa photo
                Image(
                    painter = rememberImagePainter(
                        data = mahasiswa.photoUrl,
                        builder = {
                            placeholder(R.drawable.logotpl) // Placeholder while loading
                            error(R.drawable.logotpl) // Fallback if the image cannot be loaded
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.CenterHorizontally) // Center the image
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Display mahasiswa information
                    Text(text = "Nama: ${mahasiswa.nama}")
                    Text(text = "NIM: ${mahasiswa.nomorInduk}")
                    Text(text = "Email: ${mahasiswa.email}")
                    Text(text = "Angkatan: ${mahasiswa.angkatan}")
                    Text(text = "Keterangan Lulus: ${mahasiswa.ketLulus}")

                    // Conditional information based on graduation status
                    if (mahasiswa.ketLulus == "Lulus") {
                        Text(text = "Judul TA: ${mahasiswa.judulTa}")
                        Text(text = "Dosen Pembimbing 1: ${mahasiswa.dospem1}")
                        Text(text = "Dosen Pembimbing 2: ${mahasiswa.dospem2}")
                    } else if (mahasiswa.ketLulus == "Mahasiswa") {
                        Text(text = "Semester: ${mahasiswa.semester}")
                    }

                    // Other mahasiswa details
                    Text(text = "Tempat Kerja: ${mahasiswa.tempatKerja}")
                    Text(text = "Jabatan Pekerjaan: ${mahasiswa.jabatanPekerjaan}")
                    Text(text = "Whatsapp: ${mahasiswa.whatsapp}")
                    Text(text = "Linkedln: ${mahasiswa.linkedln}")
                    Text(text = "Instagram Username: ${mahasiswa.instagramUsername}")
                    Text(text = "Facebook Username: ${mahasiswa.facebookUsername}")
                    Text(text = "Youtube Channel: ${mahasiswa.youtubeName}")
                    Text(text = "MBTI: ${mahasiswa.mbti}")

                    // Row for action buttons (Cancel and Update Password if admin)
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End // Align buttons to the end
                    ) {
                        Button(onClick = onCancel) {
                            Text("Cancel")
                        }
                        if (role == "admin") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showUpdatePasswordDialog = true },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)
                            ) {
                                Text("Update Password", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }

    // Show Update Password Dialog
    if (showUpdatePasswordDialog) {
        UpdatePasswordDialog(
            username = mahasiswa?.nama ?: "",
            newPassword = newPassword,
            onPasswordChange = { newPassword = it },
            onUpdateClick = {
                mahasiswa?.identifier?.let { id ->
                    updatePassword(context, id, newPassword) {
                        showUpdatePasswordDialog = false
                    }
                }
            },
            onCancelClick = {
                showUpdatePasswordDialog = false
            }
        )
    }
}



// Function to fetch Mahasiswa data from Firebase
fun fetchMahasiswaFromFirebase(mahasiswaRef: DatabaseReference, mahasiswaList: SnapshotStateList<Users>) {
    mahasiswaRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            mahasiswaList.clear()
            for (dataSnapshot in snapshot.children) {
                val user = dataSnapshot.getValue(Users::class.java)
                if (user != null && user.role == "mahasiswa") {
                    mahasiswaList.add(user)
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle database error
        }
    })
}


@Composable
fun Grid(
    items: List<Users>,
    itemContent: @Composable (Users) -> Unit
) {
    val columnCount = 2
    val rows = items.chunked(columnCount)

    Column {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = if (rowItems.size == 1) Arrangement.Start else Arrangement.SpaceEvenly
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f, fill = false),
                        contentAlignment = Alignment.Center
                    ) {
                        itemContent(item)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}




@Composable
fun MahasiswaCard(mahasiswa: Users, onDetailClick: (Users) -> Unit, onEditClick:(Users)->Unit, onDeleteClick:(Users)->Unit) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return


    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = { onDetailClick(mahasiswa) }),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
        ) {
            // Replace with actual image loading
            Image(
                painter = rememberImagePainter(
                    data = mahasiswa.photoUrl,
                    builder = {
                        placeholder(R.drawable.logotpl) // Placeholder while loading
                        error(R.drawable.logotpl) // Fallback if the image cannot be loaded
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
            )
            Text(text = mahasiswa.nama, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
            Text(text = mahasiswa.nomorInduk, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

            // Spacer mendorong konten di bawah (ikon) ke bagian bawah Card
            Spacer(modifier = Modifier.weight(1f))

            Row{
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onDetailClick(mahasiswa) }
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Detail")
                }

                if(role == "admin"){
                    IconButton(modifier = Modifier.weight(1f), onClick = { onEditClick(mahasiswa) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }

                    IconButton(modifier = Modifier.weight(1f), onClick = { onDeleteClick(mahasiswa) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete",   tint = Color.Red)
                    }
                }

            }

        }
    }
}


@Composable
fun AddMahasiswaForm(onSubmit: (Users, Uri?) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var angkatan by remember { mutableStateOf("") }
    var instagramUsername by remember { mutableStateOf("") }
    var facebookUsername by remember { mutableStateOf("") }
    var youtubeName by remember { mutableStateOf("") }
    var mbti by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var linkedln by remember { mutableStateOf("") }
    var KetLulus by remember { mutableStateOf("") }

    var Dospem1 by remember { mutableStateOf("") }
    var Dospem2 by remember { mutableStateOf("") }
    var JudulTA by remember { mutableStateOf("") }

    var TempatKerja by remember { mutableStateOf("") }
    var JabatanPekerjaan by remember { mutableStateOf("") }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val angkatanList = mutableListOf<String>()
    for (year in 2016..currentYear) {
        angkatanList.add(year.toString())
    }

    val mbtiOptions = listOf("INTJ", "ENTJ", "INTP", "ENTP", "INFJ", "ENFJ", "INFP", "ENFP", "ISTJ", "ESTJ", "ISFJ", "ESFJ", "ISTP", "ESTP", "ISFP", "ESFP")
    var MBTIexpanded by remember { mutableStateOf(false) }

    var angkatanExpanded by remember { mutableStateOf(false) }

    val KetLulusList = listOf("Mahasiswa", "Lulus")
    var KetLulusExpanded by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var namaIsEmpty by remember { mutableStateOf(false) }
    var nimIsEmpty by remember { mutableStateOf(false) }
    var angkatanIsEmpty by remember { mutableStateOf(false) }
    var passwordIsEmpty by remember { mutableStateOf(false) }
    var ketLulusIsEmpty by remember { mutableStateOf(false) }


    // Function to pick an image
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Add Mahasiswa",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = imageUri ?: R.drawable.logotpl,
                            builder = {
                                error(R.drawable.logotpl)
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Image upload button and preview
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "Upload Photo")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = namaIsEmpty,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nim,
                    onValueChange = { nim = it },
                    label = { Text("NIM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = nimIsEmpty,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = angkatan,
                        onValueChange = {angkatan = it },
                        label = { Text("Angkatan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { angkatanExpanded = true },
                        readOnly = true,
                        isError = angkatanIsEmpty,
                        trailingIcon = {
                            IconButton(onClick = { angkatanExpanded = true }) {
                                Icon(
                                    imageVector = if (angkatanExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = angkatanExpanded,
                        onDismissRequest = { angkatanExpanded = false }
                    ) {
                        angkatanList.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    angkatan = option
                                    angkatanExpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = KetLulus,
                        onValueChange = {KetLulus = it },
                        label = { Text("Lulus") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { KetLulusExpanded = true },
                        readOnly = true,
                        isError = ketLulusIsEmpty,
                        trailingIcon = {
                            IconButton(onClick = { KetLulusExpanded = true }) {
                                Icon(
                                    imageVector = if (KetLulusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = KetLulusExpanded,
                        onDismissRequest = { KetLulusExpanded = false }
                    ) {
                        KetLulusList.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    KetLulus = option
                                    KetLulusExpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if(KetLulus == "Lulus"){
                    OutlinedTextField(
                        value = JudulTA,
                        onValueChange = { JudulTA = it },
                        label = { Text("Judul TA") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = Dospem1,
                        onValueChange = { Dospem1 = it },
                        label = { Text("Dosen Pembimbing 1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = Dospem2,
                        onValueChange = { Dospem2 = it },
                        label = { Text("Dosen Pembimbing 2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                }

                if(KetLulus == "Mahasiswa"){
                    OutlinedTextField(
                        value = semester,
                        onValueChange = {semester = it },
                        label = { Text("Semester") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                }

                OutlinedTextField(
                    value = TempatKerja,
                    onValueChange = { TempatKerja = it },
                    label = { Text("Tempat Kerja") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = JabatanPekerjaan,
                    onValueChange = { JabatanPekerjaan = it },
                    label = { Text("Jabatan Pekerjaan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Whatsapp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = linkedln,
                    onValueChange = { linkedln = it },
                    label = { Text("Linkedln") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = instagramUsername,
                    onValueChange = { instagramUsername = it },
                    label = { Text("Instagram Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facebookUsername,
                    onValueChange = { facebookUsername = it },
                    label = { Text("Facebook Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = youtubeName,
                    onValueChange = { youtubeName = it },
                    label = { Text("YouTube Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = mbti,
                        onValueChange = { },
                        label = { Text("MBTI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { MBTIexpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { MBTIexpanded = true }) {
                                Icon(
                                    imageVector = if (MBTIexpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = MBTIexpanded,
                        onDismissRequest = { MBTIexpanded = false }
                    ) {
                        mbtiOptions.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    mbti = option
                                    MBTIexpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordIsEmpty,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            namaIsEmpty = name.isEmpty()
                            nimIsEmpty = nim.isEmpty()
                            angkatanIsEmpty = angkatan.isEmpty()
                            passwordIsEmpty = password.isEmpty()
                            ketLulusIsEmpty = KetLulus.isEmpty()


                            if(name.isEmpty() || angkatan.isEmpty() || nim.isEmpty() || password.isEmpty() || KetLulus.isEmpty() ){
                                return@Button
                            }

                            if (name.isNotBlank() && nim.isNotBlank() && angkatan.isNotBlank() && password.isNotBlank() && KetLulus.isNotBlank()) {
                                isLoading = true
                                val newUser = Users(
                                    identifier = nim,
                                    nomorInduk = nim,
                                    nama = name,
                                    angkatan = angkatan,
                                    instagramUsername = instagramUsername,
                                    facebookUsername = facebookUsername,
                                    youtubeName = youtubeName,
                                    mbti = mbti,
                                    password = password,
                                    role = "mahasiswa",
                                    jurusan =  "Teknik Perangkat Lunak",
                                    email = email,
                                    semester = semester,
                                    whatsapp = whatsapp,
                                    linkedln = linkedln,
                                    judulTa = linkedln,
                                    dospem1 = linkedln,
                                    dospem2 = linkedln,
                                    tempatKerja = linkedln,
                                    jabatanPekerjaan = linkedln,
                                    ketLulus = linkedln,
                                )
                                onSubmit(newUser, imageUri)
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Submit")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    LoadingScreen(isLoading)
}


@Composable
fun EditMahasiswaForm(mahasiswa: Users, onSubmit: (Users, Uri?) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(mahasiswa.nama) }
    var nim by remember { mutableStateOf(mahasiswa.nomorInduk) }
    var id by remember { mutableStateOf(mahasiswa.identifier) }
    var angkatan by remember { mutableStateOf(mahasiswa.angkatan) }
    var semester by remember { mutableStateOf(mahasiswa.semester) }
    var jurusan by remember { mutableStateOf(mahasiswa.jurusan) }
    var whatsapp by remember { mutableStateOf(mahasiswa.whatsapp) }
    var linkedln by remember { mutableStateOf(mahasiswa.linkedln) }
    var email by remember { mutableStateOf(mahasiswa.email) }
    var instagramUsername by remember { mutableStateOf(mahasiswa.instagramUsername) }
    var facebookUsername by remember { mutableStateOf(mahasiswa.facebookUsername) }
    var youtubeName by remember { mutableStateOf(mahasiswa.youtubeName) }
    var mbti by remember { mutableStateOf(mahasiswa.mbti) }
    var photoUrl by remember { mutableStateOf(mahasiswa.photoUrl) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var password by remember { mutableStateOf(mahasiswa.password) }
    var role by remember { mutableStateOf(mahasiswa.role) }
    var KetLulus by remember { mutableStateOf(mahasiswa.ketLulus) }

    var tempatkerja by remember { mutableStateOf(mahasiswa.tempatKerja) }
    var jabatanPekerjaan by remember { mutableStateOf(mahasiswa.jabatanPekerjaan) }

    var judulTa by remember { mutableStateOf(mahasiswa.judulTa) }
    var dospem1 by remember { mutableStateOf(mahasiswa.dospem1) }
    var dospem2 by remember { mutableStateOf(mahasiswa.dospem2) }

    var isLoading by remember { mutableStateOf(false) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val angkatanList = mutableListOf<String>()
    for (year in 2016..currentYear) {
        angkatanList.add(year.toString())
    }
    val KetLulusList = listOf("Mahasiswa", "Lulus")
    val mbtiOptions = listOf("INTJ", "ENTJ", "INTP", "ENTP", "INFJ", "ENFJ", "INFP", "ENFP", "ISTJ", "ESTJ", "ISFJ", "ESFJ", "ISTP", "ESTP", "ISFP", "ESFP")

    var MBTIexpanded by remember { mutableStateOf(false) }
    var KetLulusExpanded by remember { mutableStateOf(false) }
    var angkatanExpanded by remember { mutableStateOf(false) }

    var namaIsEmpty by remember { mutableStateOf(false) }
    var nimIsEmpty by remember { mutableStateOf(false) }
    var angkatanIsEmpty by remember { mutableStateOf(false) }
    var KetLulusIsEmpty by remember { mutableStateOf(false) }

    // Function to pick an image
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Edit Mahasiswa",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = imageUri ?: photoUrl ?: R.drawable.logotpl,
                            builder = {
                                error(R.drawable.logotpl)
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                }

                // Image upload button and preview
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "Upload Photo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = namaIsEmpty,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nim,
                    onValueChange = { nim = it },
                    label = { Text("NIM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = nimIsEmpty,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = angkatan,
                        onValueChange = {angkatan = it },
                        label = { Text("Angkatan") },
                        isError = angkatanIsEmpty,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { angkatanExpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { angkatanExpanded = true }) {
                                Icon(
                                    imageVector = if (angkatanExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = angkatanExpanded,
                        onDismissRequest = { angkatanExpanded = false }
                    ) {
                        angkatanList.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    angkatan = option
                                    angkatanExpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = KetLulus,
                        onValueChange = {KetLulus = it },
                        label = { Text("Keterangan Lulus") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { KetLulusExpanded = true },
                        readOnly = true,
                        isError = KetLulusIsEmpty,
                        trailingIcon = {
                            IconButton(onClick = { KetLulusExpanded = true }) {
                                Icon(
                                    imageVector = if (KetLulusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = KetLulusExpanded,
                        onDismissRequest = { KetLulusExpanded = false }
                    ) {
                        KetLulusList.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    KetLulus = option
                                    KetLulusExpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if(KetLulus == "Mahasiswa"){
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if(KetLulus == "Lulus"){
                    OutlinedTextField(
                        value = judulTa,
                        onValueChange = { judulTa = it },
                        label = { Text("Judul TA") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dospem1,
                        onValueChange = { dospem1 = it },
                        label = { Text("Dosen Pembimbing 1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dospem2,
                        onValueChange = { dospem2 = it },
                        label = { Text("Dosen Pembimbing 2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = tempatkerja,
                    onValueChange = { tempatkerja = it },
                    label = { Text("Tempat Kerja") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jabatanPekerjaan,
                    onValueChange = { jabatanPekerjaan = it },
                    label = { Text("Jabatan Pekerjaan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Whatsapp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = linkedln,
                    onValueChange = { linkedln = it },
                    label = { Text("Linkedln") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = instagramUsername,
                    onValueChange = { instagramUsername = it },
                    label = { Text("Instagram Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = facebookUsername,
                    onValueChange = { facebookUsername = it },
                    label = { Text("Facebook Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = youtubeName,
                    onValueChange = { youtubeName = it },
                    label = { Text("YouTube Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = mbti,
                        onValueChange = {mbti = it },
                        label = { Text("MBTI") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { MBTIexpanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { MBTIexpanded = true }) {
                                Icon(
                                    imageVector = if (MBTIexpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = MBTIexpanded,
                        onDismissRequest = { MBTIexpanded = false }
                    ) {
                        mbtiOptions.forEach { option ->
                            DropdownMenuItem(
                                text =  {Text(text = option)},
                                onClick = {
                                    mbti = option
                                    MBTIexpanded = false
                                })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(Color.Red)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            namaIsEmpty = name.isEmpty()
                            nimIsEmpty = nim.isEmpty()
                            angkatanIsEmpty = angkatan.isEmpty()
                            KetLulusIsEmpty = KetLulus.isEmpty()

                            if(name.isEmpty() || nim.isEmpty() || angkatan.isEmpty() || KetLulusIsEmpty){
                                return@Button
                            }

                            if (name.isNotBlank() && nim.isNotBlank() && angkatan.isNotBlank() && KetLulus.isNotBlank()) {
                                isLoading = true
                                val updatedMahasiswa = Users(
                                    identifier = id,
                                    nomorInduk = nim,
                                    nama = name,
                                    angkatan = angkatan,
                                    jurusan = jurusan,
                                    semester = semester,
                                    instagramUsername = instagramUsername,
                                    facebookUsername = facebookUsername,
                                    youtubeName = youtubeName,
                                    mbti = mbti,
                                    password = password,
                                    photoUrl = photoUrl,
                                    role = role,
                                    whatsapp = whatsapp,
                                    email = email,
                                    linkedln = linkedln,
                                    dospem1 = dospem1,
                                    judulTa = judulTa,
                                    dospem2 = dospem2,
                                    tempatKerja = tempatkerja,
                                    jabatanPekerjaan = jabatanPekerjaan,
                                    ketLulus = KetLulus,
                                )
                                onSubmit(updatedMahasiswa, imageUri)
                            }
                        },
                        enabled = !isLoading
                    ) {

                            Text("Submit")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    LoadingScreen(isLoading)
}


