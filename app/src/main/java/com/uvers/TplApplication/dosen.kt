package com.uvers.TplApplication

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uvers.TplApplication.control.LoadingScreen
import com.uvers.TplApplication.control.UpdatePasswordDialog
import com.uvers.TplApplication.control.addUser
import com.uvers.TplApplication.control.deleteUser
import com.uvers.TplApplication.control.fetchUser
import com.uvers.TplApplication.control.getSavedCredentials
import com.uvers.TplApplication.control.updatePassword
import com.uvers.TplApplication.control.updateUser
import com.uvers.TplApplication.models.Users
import kotlinx.coroutines.launch

@Composable
fun DosenMain() {
    val DosenNavController = rememberNavController()

    NavHost(DosenNavController, startDestination = "dosenList") {
        composable("dosenList") {
            DosenListScreen(DosenNavController)
        }
        composable("add") {
            AddDosenScreen(DosenNavController)
        }
        composable(
            "edit/{dosenId}",
            arguments = listOf(navArgument("dosenId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dosenId = backStackEntry.arguments?.getString("dosenId")
            if (dosenId != null) {
                var dosen by remember { mutableStateOf<Users?>(null) }

                LaunchedEffect(dosenId) {
                    fetchUser(dosenId) { fetchedDosen ->
                        dosen = fetchedDosen
                    }
                }

                dosen?.let { dosen ->
                    EditDosenScreen(
                        dosen = dosen,
                        onSubmit = { updatedDosen, imageUri ->
                            updateUser(updatedDosen, imageUri, onComplete = {
                                DosenNavController.popBackStack()
                            })
                        },
                        onCancel = {
                            DosenNavController.popBackStack()
                        }
                    )
                }
            }
        }

        composable(
            "detail/{dosenId}",
            arguments = listOf(navArgument("dosenId") { type = NavType.StringType })

        ) { backStackEntry ->
            val dosenId = backStackEntry.arguments?.getString("dosenId")
            if(dosenId != null){
                var dosen by remember { mutableStateOf<Users?>(null) }

                LaunchedEffect(dosenId) {
                    fetchUser(dosenId) { fetchdosen ->
                        dosen = fetchdosen
                    }
                }

                dosen?.let{dosen ->
                    DetailDosenDialog(DosenNavController, dosen = dosen, onCancel = {
                        DosenNavController.popBackStack()
                    })
                }
            }
        }

    }
}

@Composable
fun DosenListScreen(navController: NavController) {
    var dosenList by remember { mutableStateOf<List<Users>>(emptyList()) }
    var selectedDosen by remember { mutableStateOf<Users?>(null) }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return


    // Fetch the dosen list from Firebase
    LaunchedEffect(Unit) {
        fetchDosenList { fetchedList ->
            dosenList = fetchedList
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if(role == "admin"){
            item {
                Row( modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    Button(onClick = { navController.navigate("add") }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Dosen")
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(dosenList) { dosen ->
            DosenCard(
                dosen = dosen,
                onDetailClick = {
                    navController.navigate("detail/${dosen.identifier}")
                },
                onEditClick = { navController.navigate("edit/${dosen.identifier}") },
                onDeleteClick = {
                    selectedDosen = dosen
                    showDialog = true
                }
            )
        }



    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${selectedDosen?.nama}?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            selectedDosen?.let {
                                dosenList = dosenList.filter { it.identifier != selectedDosen?.identifier }
                                deleteUser(selectedDosen!!.identifier) { success ->
                                    if (success) {
                                        fetchDosenList { fetchedList ->
                                            dosenList = fetchedList
                                        }
                                    }
                                }
                            }
                            showDialog = false
                            selectedDosen = null
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DosenCard(
    dosen: Users,
    onDetailClick: (Users) -> Unit,
    onEditClick: (Users) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable(onClick = { onDetailClick(dosen) }),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = dosen.photoUrl ?: R.drawable.logotpl,
                    builder = {
                        error(R.drawable.logotpl)
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dosen.nama, fontSize = 20.sp)
                Text(text = dosen.role, fontSize = 16.sp)
            }
            Column {
                IconButton(onClick = { onDetailClick(dosen) }) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Detail")
                }
                if(role == "admin"){
                    IconButton(onClick = { onEditClick(dosen) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Yellow)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete",  tint = Color.Red)
                    }
                }

            }
        }
    }
}

@Composable
fun AddDosenScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) } // Add this state

    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var nomorInduk by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var instagramUsername by remember { mutableStateOf("") }
    var facebookUsername by remember { mutableStateOf("") }
    var mbti by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var linkedln by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var youtubeName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var nameisEmpty by remember { mutableStateOf(false) }
    var idisEmpty by remember { mutableStateOf(false) }
    var passwordEmpty by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    val mbtiOptions = listOf("INTJ", "ENTJ", "INTP", "ENTP", "INFJ", "ENFJ", "INFP", "ENFP", "ISTJ", "ESTJ", "ISFJ", "ESFJ", "ISTP", "ESTP", "ISFP", "ESFP")
    var MBTIexpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        contentScale = ContentScale.Crop
                    )
                } ?: Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Select Image",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }
        }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameisEmpty,
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = nomorInduk,
                onValueChange = { nomorInduk = it },
                label = { Text("NIDN") },
                modifier = Modifier.fillMaxWidth(),
                isError = idisEmpty,
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                isError = passwordEmpty,
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("Whatsapp Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = linkedln,
                onValueChange = { linkedln = it },
                label = { Text("Linkedln") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = instagramUsername,
                onValueChange = { instagramUsername = it },
                label = { Text("Instagram Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = facebookUsername,
                onValueChange = { facebookUsername = it },
                label = { Text("Facebook Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        item {
            OutlinedTextField(
                value = youtubeName,
                onValueChange = { youtubeName = it },
                label = { Text("YouTube Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
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
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row{
                Button(
                    onClick = {navController.popBackStack()},
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(Color.Red)){
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        nameisEmpty = name.isEmpty()
                        idisEmpty = nomorInduk.isEmpty()
                        passwordEmpty = password.isEmpty()

                        if (name.isEmpty() || nomorInduk.isEmpty() || password.isEmpty()) {
                            return@Button
                        }

                        isLoading = true

                        val newDosen = Users(
                            identifier = nomorInduk,
                            nomorInduk = nomorInduk,
                            nama = name,
                            angkatan = "",
                            jurusan = "Teknik Perangkat Lunak",
                            semester = "",
                            instagramUsername = instagramUsername,
                            facebookUsername = facebookUsername,
                            youtubeName = youtubeName,
                            mbti = mbti,
                            photoUrl = imageUrl,
                            password = password,
                            role = "dosen",
                            whatsapp = whatsapp,
                            linkedln = linkedln,
                            email = email,
                        )
                        scope.launch {
                            addUser(newDosen, imageUri) { success ->
                                if (success) {
                                    isLoading = false
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                ) {
                    Text("Add")
                }

            }

        }
    }
    LoadingScreen(isLoading)
}

@Composable
fun EditDosenScreen(
    dosen: Users,
    onSubmit: (Users, Uri?) -> Unit,
    onCancel: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(dosen.nama) }
    var id by remember { mutableStateOf(dosen.identifier) }
    var nomorinduk by remember { mutableStateOf(dosen.nomorInduk) }
    var password by remember { mutableStateOf(dosen.password) }
    var instagramUsername by remember { mutableStateOf(dosen.instagramUsername) }
    var facebookUsername by remember { mutableStateOf(dosen.facebookUsername) }
    var mbti by remember { mutableStateOf(dosen.mbti) }
    var whatsapp by remember { mutableStateOf(dosen.whatsapp) }
    var youtubeName by remember { mutableStateOf(dosen.youtubeName) }
    var linkedln by remember { mutableStateOf(dosen.linkedln) }
    var email by remember { mutableStateOf(dosen.email) }
    var imageUrl by remember { mutableStateOf(dosen.photoUrl) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    val mbtiOptions = listOf("INTJ", "ENTJ", "INTP", "ENTP", "INFJ", "ENFJ", "INFP", "ENFP", "ISTJ", "ESTJ", "ISFJ", "ESFJ", "ISTP", "ESTP", "ISFP", "ESFP")
    var MBTIexpanded by remember { mutableStateOf(false) }


    var nameisEmpty by remember { mutableStateOf(false) }
    var idisEmpty by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = imageUri ?: imageUrl ?: R.drawable.logotpl,
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
        }

        item {
            OutlinedTextField(
                value = nomorinduk,
                onValueChange = { nomorinduk = it },
                singleLine = true,
                label = { Text("NIDN") },
                modifier = Modifier.fillMaxWidth(),
                isError = idisEmpty
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameisEmpty
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("Whatsapp Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = linkedln,
                onValueChange = { linkedln = it },
                label = { Text("Linkedln") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }


        item {
            OutlinedTextField(
                value = instagramUsername,
                onValueChange = { instagramUsername = it },
                label = { Text("Instagram Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = facebookUsername,
                onValueChange = { facebookUsername = it },
                label = { Text("Facebook Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }



        item {
            OutlinedTextField(
                value = youtubeName,
                onValueChange = { youtubeName = it },
                label = { Text("YouTube Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Box {
                OutlinedTextField(
                    value = mbti,
                    onValueChange = {mbti = it },
                    label = { Text("MBTI") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { MBTIexpanded = true },
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
                            text = {Text(text = option)},
                            onClick = {
                            mbti = option
                            MBTIexpanded = false
                        })
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row{
                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(Color.Red)) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        nameisEmpty = name.isEmpty()
                        idisEmpty = nomorinduk.isEmpty()


                        if (name.isEmpty() || nomorinduk.isEmpty()) {
                            // Handle empty fields
                            return@Button
                        }

                        isLoading = true

                        val updatedDosen = dosen.copy(
                            nama = name,
                            instagramUsername = instagramUsername,
                            facebookUsername = facebookUsername,
                            mbti = mbti,
                            angkatan = "",
                            semester = "",
                            jurusan = "Teknik Perangkat Lunak",
                            whatsapp = whatsapp,
                            youtubeName = youtubeName,
                            password = password,
                            photoUrl = imageUrl,
                            role = "dosen",
                            identifier = id,
                            nomorInduk = nomorinduk,
                            email = email,
                            linkedln = linkedln,
                        )
                        scope.launch {
                            onSubmit(updatedDosen, imageUri)
                        }
                    }
                ) {
                    Text("Update")
                }

            }


        }
    }
    LoadingScreen(isLoading)
}

@Composable
fun DetailDosenDialog(navController: NavController, dosen: Users, onCancel: () -> Unit) {

    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    var showUpdatePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }


    dosen?.let {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = it.photoUrl,
                        builder = {
                            placeholder(R.drawable.logotpl) // Placeholder while loading
                            error(R.drawable.logotpl) // Fallback if the image cannot be loaded
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Name: ${it.nama}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "NIDN:  ${it.nomorInduk}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Email:  ${it.email}", style = MaterialTheme.typography.bodyMedium)

                if(it.whatsapp.isNotEmpty()) {
                    Text(text = "Whatsapp: ${it.whatsapp}", style = MaterialTheme.typography.bodyMedium)
                }

                if(it.linkedln.isNotEmpty()) {
                    Text(text = "Linkedln: ${it.linkedln}", style = MaterialTheme.typography.bodyMedium)
                }

                if(it.instagramUsername.isNotEmpty()){
                    Text(text = "Instagram: ${it.instagramUsername}", style = MaterialTheme.typography.bodyMedium)
                }

                if(it.facebookUsername.isNotEmpty()) {
                    Text(text = "Facebook: ${it.facebookUsername}", style = MaterialTheme.typography.bodyMedium)
                }

                if(it.youtubeName.isNotEmpty()) {
                    Text(text = "YouTube: ${it.youtubeName}", style = MaterialTheme.typography.bodyMedium)
                }

                if(it.mbti.isNotEmpty()){
                    Text(text = "MBTI: ${it.mbti}", style = MaterialTheme.typography.bodyMedium)
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                Row{
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                if(role == "admin"){
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        showUpdatePasswordDialog = true
                    },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)
                        ) {
                        Text("Update Password", color=MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

        }
        }
    }

    if (showUpdatePasswordDialog) {
        UpdatePasswordDialog(
            username = dosen.nama,
            newPassword = newPassword,
            onPasswordChange = { newPassword = it },
            onUpdateClick = {

                updatePassword(context, dosen.identifier, newPassword) {
                    showUpdatePasswordDialog = false
                }
            },
            onCancelClick = {
                showUpdatePasswordDialog = false
            }
        )
    }
}

// Helper functions for Firebase operations
fun fetchDosenList(onDosenListFetched: (List<Users>) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val dosenRef = database.getReference("Users")

    dosenRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val dosenList = mutableListOf<Users>()
            for (dataSnapshot in snapshot.children) {
                val dosen = dataSnapshot.getValue(Users::class.java)
                if (dosen?.role == "dosen") {
                    dosenList.add(dosen)
                }
            }
            onDosenListFetched(dosenList)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle possible errors.
        }
    })
}

