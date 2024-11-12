package com.uvers.TplApplication

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.uvers.TplApplication.control.LoadingScreen
import com.uvers.TplApplication.control.getSavedCredentials
import com.uvers.TplApplication.control.saveCredentials

@Composable
fun LoginScreen(navController: NavHostController,  activity: ComponentActivity) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveCredentials by remember { mutableStateOf(true) }
    var loginError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var missingPass by remember { mutableStateOf(false) }
    var missingId by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check if credentials are already saved
    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials != null) {
            identifier = savedCredentials.first
            navController.navigate("Home")
        }
    }

    BackHandler {
        activity.finish() // Close the app
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val logoPainter: Painter = painterResource(id = R.drawable.logotpl)
                Image(
                    painter = logoPainter,
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = "Welcome To\nTeknik Perangkat Lunak,\nUniversitas Universal",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 20.dp),
                    color = colorScheme.onSurface
                )
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                    },
                    label = { Text("Username", color = colorScheme.onSurface) },
                    shape = RoundedCornerShape(26.dp),
                    singleLine = true,
                    isError = missingId,
                    colors = TextFieldDefaults.colors(
                        focusedLeadingIconColor = colorScheme.primary,
                        unfocusedLeadingIconColor = colorScheme.onSurface,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurface,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface,
                        focusedPlaceholderColor = colorScheme.onSurface,
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Username")
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = { Text("Password",  color = colorScheme.onSurface) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(26.dp),
                    singleLine = true,
                    isError = missingPass,
                    colors = TextFieldDefaults.colors(
                        focusedLeadingIconColor = colorScheme.primary,
                        unfocusedLeadingIconColor = colorScheme.onSurface,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.onSurface,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.onSurface,
                        focusedPlaceholderColor = colorScheme.onSurface,
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        Icon(imageVector = image, contentDescription = null, modifier = Modifier.clickable {
                            passwordVisible = !passwordVisible
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
                if (loginError) {
                    Text(
                        text = "Invalid username or password",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        missingId = identifier.isEmpty()
                        missingPass = password.isEmpty()

                        if(identifier.isEmpty() || password.isEmpty()){
                            return@Button
                        }
                        if (identifier.isNotEmpty() && password.isNotEmpty()) {
                            isLoading = true
                            authenticateWithFirebase(context, identifier, password, saveCredentials, navController) { success ->
                                isLoading = false
                                loginError = !success
                            }
                        } else {
                            loginError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, start = 40.dp, end = 40.dp, bottom = 20.dp)
                ) {
                    Text("Login")
                }
            }

    }

    LoadingScreen(isLoading)
}
private fun authenticateWithFirebase(
    context: Context,
    identifier: String,  // Ini diasumsikan sebagai nomorInduk atau NIM
    password: String,
    saveCredentials: Boolean,
    navController: NavController,
    onResult: (Boolean) -> Unit
) {
    val database = Firebase.database
    val usersRef = database.getReference("Users")
    Log.d("Login", "authenticateWithFirebase")

    // Gunakan orderByChild untuk mencari berdasarkan field "nomorInduk"
    val query = usersRef.orderByChild("nomorInduk").equalTo(identifier)
    query.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            Log.d("Login", "User exists")
            // Karena query mungkin mengembalikan beberapa hasil, lakukan loop
            for (userSnapshot in snapshot.children) {
                val userPassword = userSnapshot.child("password").value
                if (userPassword == password) {
                    Log.d("Login", "Password matches")
                    if (saveCredentials) {
                        val role = userSnapshot.child("role").value?.toString() ?: ""
                        saveCredentials(context, identifier, role)
                    }
                    navController.navigate("Home")
                    onResult(true)
                    return@addOnSuccessListener // Keluar setelah user ditemukan
                } else {
                    Log.d("Login", "Password does not match")
                    onResult(false)
                    return@addOnSuccessListener
                }
            }
        } else {
            Log.d("Login", "User does not exist")
            onResult(false)
        }
    }.addOnFailureListener {
        Log.e("Login", "Error fetching user data", it)
        onResult(false)
    }
}
