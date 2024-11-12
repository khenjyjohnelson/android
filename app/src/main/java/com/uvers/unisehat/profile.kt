package com.uvers.unisehat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.uvers.unisehat.control.LoadingScreen
import com.uvers.unisehat.control.clearCredentials
import com.uvers.unisehat.control.fetchUser
import com.uvers.unisehat.control.getSavedCredentials
import com.uvers.unisehat.control.updatePassword
import com.uvers.unisehat.control.updateUser
import com.uvers.unisehat.models.AboutUs
import com.uvers.unisehat.models.Users

@Composable
fun ProfileMain(MainNavController: NavHostController) {
    val ProfileNavController = rememberNavController()

    var user by remember { mutableStateOf<Users?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            val (identifier, _) = savedCredentials
            fetchUser(identifier) {
                user = it
            }
        }
    }

    NavHost(
        navController = ProfileNavController,
        startDestination = "profile_root"
    ) {
        composable("profile_root") {
            ProfileRootScreen(ProfileNavController, MainNavController)
        }
        composable("profile_detail") {
            ProfileDetailScreen(ProfileNavController)
        }
        composable("profile_update") {
            ProfileUpdateScreen(ProfileNavController)
        }
        composable("about") {
            AboutUsScreen(ProfileNavController)
        }
        composable("update_about") {
            UpdateAboutUsScreen(ProfileNavController)
        }
    }
}


@Composable
fun ProfileRootScreen(navController: NavController, MainNavController: NavHostController) {
    var user by remember { mutableStateOf<Users?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            val (identifier, _) = savedCredentials
            fetchUser(identifier) {
                user = it
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),  // Make space for the profile picture
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(50.dp))  // Space for the profile picture
                user?.let {
                    Text(text = it.nama, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = buildString {
                            append(if (it.role == "mahasiswa") "NIM : " else "NIDN : ")
                            append(it.nomorInduk)
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ProfileMenuItem(title = "Profile", icon = Icons.Default.Person, navController, MainNavController)
                ProfileMenuItem(title = "About Us", icon = Icons.Default.Info, navController, MainNavController)
                ProfileMenuItem(title = "Logout", icon = Icons.Default.ExitToApp, navController, MainNavController)
            }
        }

        // Profile picture overlaps the card
        user?.let {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)  // Clip the image into a circle
                    .background(Color.Gray)  // Fallback color if image is not available
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp) // Adjust padding to overlap with the card
            ) {
                val painter = rememberImagePainter(
                    data = it.photoUrl,
                    builder = {
                        placeholder(R.drawable.logotpl)
                        error(R.drawable.logotpl)
                    }
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, navController: NavController, MainNavController: NavHostController) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                when (title) {
                    "Profile" -> navController.navigate("profile_detail")
                    "About Us" -> navController.navigate("about")
                    "Logout" -> {
                        clearCredentials(context)
                        MainNavController.navigate("Login") {
                            popUpTo(0)
                        }
                    }
                }
            }
            .padding(horizontal = 16.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp)
    }
}



@Composable
fun ProfileDetailScreen(navController: NavController) {
    var user by remember { mutableStateOf<Users?>(null) }
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            val (identifier, _) = savedCredentials
            fetchUser(identifier) {
                user = it
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        user?.let {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)  // Clip the image into a circle
                    .background(Color.Gray)  // Fallback color if image is not available
                    .align(Alignment.CenterHorizontally)
                    .border(1.dp, color = Color.Black)
            ) {
                val painter = rememberImagePainter(
                    data = it.photoUrl,
                    builder = {
                        placeholder(R.drawable.logotpl)
                        error(R.drawable.logotpl)
                    }
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                        .border(1.dp, color = Color.Black)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailItem(label = "Nama", value = it.nama)
            ProfileDetailItem(label = if (it.role == "mahasiswa") "NIM" else "NIDN", value = it.nomorInduk)

            if(it.role == "mahasiswa"){
                ProfileDetailItem(label = "Jurusan", value = it.jurusan)
                ProfileDetailItem(label = "Angkatan", value = it.angkatan)

                if(it.ketLulus == "Mahasiswa"){
                    ProfileDetailItem(label = "Semester", value = it.semester)
                }
                if(it.ketLulus == "Lulus"){
                    ProfileDetailItem(label = "Judul Ta", value = it.judulTa)
                    ProfileDetailItem(label = "Dosen Pembimbing 1", value = it.dospem1)
                    ProfileDetailItem(label = "Dosen Pembimbing 2", value = it.dospem2)
                }

                ProfileDetailItem(label = "Tempat Kerja", value = it.jurusan)
                ProfileDetailItem(label = "Jabatan Pekerjaan", value = it.jabatanPekerjaan)

            }
            ProfileDetailItem(label = "Email", value = it.email)
            ProfileDetailItem(label = "Whatsapp", value = it.whatsapp)
            ProfileDetailItem(label = "Linkedln Username", value = it.linkedln)
            ProfileDetailItem(label = "Instagram Username", value = it.instagramUsername)
            ProfileDetailItem(label = "Facebook Username", value = it.facebookUsername)
            ProfileDetailItem(label = "Youtube Channel", value = it.youtubeName)
            ProfileDetailItem(label = "MBTI", value = it.mbti)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row{
            Button(onClick = { navController.navigate("profile_update") }) {
                Text("Update Profile")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = { showChangePasswordDialog = true },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)) {
                Text("Change Password", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onPasswordChange = { oldPassword, newPassword ->
                // Handle password change logic here
                user?.let {
                    validateOldPassword(it.identifier, oldPassword) { isValid ->
                        if (isValid) {
                            updatePassword(context, it.identifier, newPassword) {
                                showChangePasswordDialog = false
                            }
                        } else {
                            // Show error message
                            Toast.makeText(context, "Old password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChange: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var newpasswordVisible by remember { mutableStateOf(false) }

    var newpasswordIsEmpty by remember { mutableStateOf(false) }
    var oldpasswordIsEmpty by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = image, contentDescription = null, modifier = Modifier.clickable {
                            passwordVisible = !passwordVisible
                        })
                    },
                    isError = oldpasswordIsEmpty
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation =if (newpasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (newpasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = image, contentDescription = null, modifier = Modifier.clickable {
                            newpasswordVisible = !newpasswordVisible
                        })
                    },
                    isError = newpasswordIsEmpty
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    newpasswordIsEmpty = newPassword.isEmpty()
                    oldpasswordIsEmpty = oldPassword.isEmpty()

                    if(newPassword.isEmpty() || oldPassword.isEmpty()){
                        return@Button
                    }
                    if (!newpasswordIsEmpty && !oldpasswordIsEmpty){
                        onPasswordChange(oldPassword, newPassword)
                    }
                }
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun ProfileUpdateScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(false) } // Add this state
    var name by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var programStudi by remember { mutableStateOf("") }
    var angkatan by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var mbti by remember { mutableStateOf("") }
    var instagramUsername by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var fbusername by remember { mutableStateOf("") }
    var linkedln by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var imageurl by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var youtubename by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var nomorInduk by remember { mutableStateOf("") }
    var ketlulus by remember { mutableStateOf("") }
    var judulTa by remember { mutableStateOf("") }
    var dospem1 by remember { mutableStateOf("") }
    var dospem2 by remember { mutableStateOf("") }
    var tempatKerja by remember { mutableStateOf("") }
    var Jabatankerja by remember { mutableStateOf("") }
    val context = LocalContext.current

    val mbtiOptions = listOf("INTJ", "ENTJ", "INTP", "ENTP", "INFJ", "ENFJ", "INFP", "ENFP", "ISTJ", "ESTJ", "ISFJ", "ESFJ", "ISTP", "ESTP", "ISFP", "ESFP")
    var MBTIexpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }


    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            val (identifier, _) = savedCredentials
            fetchUser(identifier) { user ->
                if (user != null) {
                    name = user.nama
                    nomorInduk = user.nomorInduk
                    nim = user.identifier
                    programStudi = user.jurusan
                    mbti = user.mbti
                    angkatan = user.angkatan
                    instagramUsername = user.instagramUsername
                    fbusername = user.facebookUsername
                    role = user.role
                    imageurl = user.photoUrl
                    password = user.password
                    youtubename = user.youtubeName
                    semester = user.semester
                    whatsapp = user.whatsapp
                    linkedln = user.linkedln
                    email = user.email
                    ketlulus = user.ketLulus
                    judulTa = user.judulTa
                    dospem1 = user.dospem1
                    dospem2 = user.dospem2
                    tempatKerja = user.tempatKerja
                    Jabatankerja = user.jabatanPekerjaan
                }
            }
        }
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, shape = CircleShape)
                    .clickable { launcher.launch("image/*") }
                    .clip(CircleShape)

            ) {
                Image(
                    painter = rememberImagePainter(
                        data = imageUri ?: imageurl ?: R.drawable.logotpl,
                        builder = {
                            error(R.drawable.logotpl)
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)

                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nomorInduk,
            onValueChange = { nomorInduk = it },
            label = { Text(if (role == "mahasiswa") "NIM" else "NIDN") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )


        if(role == "mahasiswa"){
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = programStudi,
                onValueChange = { programStudi = it },
                label = { Text("Jurusan") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
            )

            if(ketlulus == "Mahasiswa"){
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                )
            }

            if(ketlulus == "Lulus"){
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = judulTa,
                    onValueChange = { judulTa = it },
                    label = { Text("Judul TA") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dospem1,
                    onValueChange = { dospem1 = it },
                    label = { Text("Dosen Pembimbing 1") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dospem2,
                    onValueChange = { dospem2 = it },
                    label = { Text("Dosen Pembimbing 2") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tempatKerja,
                onValueChange = { tempatKerja = it },
                label = { Text("Tempat Kerja") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = Jabatankerja,
                onValueChange = { Jabatankerja = it },
                label = { Text("Jabatan Pekerjaan") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("Whatsapp") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = linkedln,
            onValueChange = { linkedln = it },
            label = { Text("Linkedln Username") },
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
            value = fbusername,
            onValueChange = { fbusername = it },
            label = { Text("Facebook Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = youtubename,
            onValueChange = { youtubename = it },
            label = { Text("Youtube Channel") },
            modifier = Modifier.fillMaxWidth(),
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
        Button(onClick = {
            isLoading = true
            // Implement save logic here
            val updatedUser = Users(
                nama = name,
                identifier = nim,
                nomorInduk = nomorInduk,
                jurusan = programStudi,
                angkatan = angkatan,
                mbti = mbti,
                instagramUsername = instagramUsername,
                facebookUsername = fbusername,
                role = role,
                semester = semester,
                photoUrl = imageurl,
                password = password,
                youtubeName = youtubename,
                whatsapp = whatsapp,
                linkedln = linkedln,
                email = email,
                dospem1 = dospem1,
                dospem2 = dospem2,
                judulTa = judulTa,
                tempatKerja = tempatKerja,
                jabatanPekerjaan = Jabatankerja,
                ketLulus = ketlulus,
            )
            updateUser(updatedUser, imageUri) {
                isLoading = false
                navController.popBackStack() // Navigate back to the previous screen
            }

        }) {
            Text("Save")
        }
    }

    LoadingScreen(isLoading)
}


@Composable
fun AboutUsScreen(navController: NavController) {
    val context = LocalContext.current
    val (identifier, role) = getSavedCredentials(context) ?: return

    var aboutUs by remember { mutableStateOf<AboutUs?>(null) }

    LaunchedEffect(Unit) {
        val database = Firebase.database
        val aboutUsRef = database.getReference("AboutUs").child("1")

        aboutUsRef.get().addOnSuccessListener { snapshot ->
            aboutUs = snapshot.getValue(AboutUs::class.java)
            if (aboutUs == null) {
                // Create default About Us data if it doesn't exist
                val defaultAboutUs = AboutUs(description = "This is a description about the app or the organization. Here you can put any relevant information.")
                aboutUsRef.setValue(defaultAboutUs)
                aboutUs = defaultAboutUs
            }
        }.addOnFailureListener {
            // Handle the error here
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "About Us",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        aboutUs?.let {
            Text(
                text = it.description,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ){
            Button(
                onClick = {navController.popBackStack()},
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
            ){
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if(role == "admin"){
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { navController.navigate("update_about") },
                ) {
                    Text("Update")
                }
            }
        }



    }
}

@Composable
fun UpdateAboutUsScreen(navController: NavController) {
    var aboutUsText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Add this state

    val context = LocalContext.current


    LaunchedEffect(Unit) {
        val database = Firebase.database
        val aboutUsRef = database.getReference("AboutUs").child("1")

        aboutUsRef.get().addOnSuccessListener { snapshot ->
            val aboutUs = snapshot.getValue(AboutUs::class.java)
            aboutUsText = aboutUs?.description ?: ""
        }.addOnFailureListener {
            // Handle the error here
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = aboutUsText,
            onValueChange = { aboutUsText = it },
            label = { Text("About Us") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            isLoading = true
            val database = Firebase.database
            val aboutUsRef = database.getReference("AboutUs").child("1")

            // Update About Us data
            val updatedAboutUs = AboutUs(description = aboutUsText)
            aboutUsRef.setValue(updatedAboutUs).addOnSuccessListener {
                isLoading = false
                navController.popBackStack() // Navigate back to the previous screen
            }.addOnFailureListener {
                // Handle the error here
            }
        }) {
            Text("Save")
        }
    }

    LoadingScreen(isLoading)
}

fun validateOldPassword(identifier: String, oldPassword: String, callback: (Boolean) -> Unit) {
    val databaseReference = Firebase.database.getReference("Users/$identifier")
    databaseReference.child("password").get().addOnSuccessListener { snapshot ->
        val savedPassword = snapshot.value as? String
        callback(oldPassword == savedPassword)
    }.addOnFailureListener {
        callback(false)
    }
}
