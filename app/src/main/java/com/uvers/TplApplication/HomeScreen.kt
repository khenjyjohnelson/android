package com.uvers.TplApplication

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uvers.TplApplication.control.clearCredentials
import com.uvers.TplApplication.control.getSavedCredentials
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(MainNavController: NavHostController) {
    val HomeNavController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    val selected = remember { mutableStateOf(Icons.Default.Home) }
    val drawerSelection = remember { mutableStateOf("") }
    var backPressedOnce by remember { mutableStateOf(false) }
    val activity = (LocalContext.current as? ComponentActivity)

    LaunchedEffect(Unit) {
        val savedCredentials = getSavedCredentials(context)
        if (savedCredentials == null) {
            MainNavController.navigate("login")
        }
    }
    
    BackHandler {
        if (backPressedOnce) {
            // Jika tombol kembali ditekan dua kali, hapus kredensial dan navigasi ke login
            // Hapus kredensial sesuai kebutuhan
            clearCredentials(context)

            // Navigasi ke login screen
            MainNavController.navigate("Login") {
                popUpTo("Home") { inclusive = true }
            }
        } else {
            // Tampilkan toast dan tunggu untuk klik kedua
            backPressedOnce = true
            Toast.makeText(activity, "Tekan sekali lagi untuk keluar.", Toast.LENGTH_SHORT).show()

            // Reset state jika tidak diklik lagi dalam 2 detik
            coroutineScope.launch {
                delay(2000L)
                backPressedOnce = false
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .height(70.dp)
                        .fillMaxWidth()
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "Home") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "home") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Home.screens){
                            popUpTo(0)
                        }
                        selected.value = Icons.Default.Home
                        drawerSelection.value = "Home"
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp)) // Divider between Jadwal and Logout

                NavigationDrawerItem(
                    label = { Text(text = "Mahasiswa") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "mahasiswa") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Mahasiswa.screens) {
                            popUpTo(0)
                        }
                        drawerSelection.value = "Mahasiswa"
                        selected.value = Icons.Default.Person
                    }
                )

                NavigationDrawerItem(
                    label = { Text(text = "Dosen") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "dosen") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Dosen.screens){
                            popUpTo(0)
                        }
                        drawerSelection.value = "Dosen"
                        selected.value = Icons.Default.Person
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp)) // Divider between Jadwal and Logout

                NavigationDrawerItem(
                    label = { Text(text = "Agenda") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "agenda") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Agenda.screens){
                            popUpTo(0)
                        }
                        selected.value = Icons.Default.DateRange
                        drawerSelection.value = "Agenda"
                    }
                )

                NavigationDrawerItem(
                    label = { Text(text = "Jadwal") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Jadwal") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Jadwal.screens){
                            popUpTo(0)
                        }
                        drawerSelection.value = "Jadwal"
                        selected.value = Icons.Default.Person
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp)) // Divider between Jadwal and Logout

                NavigationDrawerItem(
                    label = { Text(text = "Profile") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "profile") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        HomeNavController.navigate(Screens.Profile.screens){
                            popUpTo(0)
                        }
                        selected.value = Icons.Default.AccountCircle
                        drawerSelection.value = "Profile"
                    }
                )

                NavigationDrawerItem(
                    label = { Text(text = "Logout") },
                    selected = false,
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "logout") },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        clearCredentials(context)
                        Toast.makeText(context, "Logout From App", Toast.LENGTH_SHORT).show()
                        MainNavController.navigate("Login"){
                            popUpTo("Home") { inclusive = true }
                        }
                        selected.value = Icons.Default.ExitToApp
                        drawerSelection.value = "Logout"
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Teknik Perangkat Lunak, UVERS") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Menu, contentDescription = "MenuButton"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                    NavigationBarItem(
                        selected = selected.value == Icons.Default.Home,
                        onClick = {
                            selected.value = Icons.Default.Home
                            drawerSelection.value = "Home"
                            HomeNavController.navigate(Screens.Home.screens){
                                popUpTo(0)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = selected.value == Icons.Default.DateRange,
                        onClick = {
                            selected.value = Icons.Default.DateRange
                            drawerSelection.value = "Agenda"
                            HomeNavController.navigate(Screens.Agenda.screens){
                                popUpTo(0)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Agenda") }
                    )

                    NavigationBarItem(
                        selected = selected.value == Icons.Default.AccountCircle,
                        onClick = {
                            selected.value = Icons.Default.AccountCircle
                            drawerSelection.value = "Profile"
                            HomeNavController.navigate(Screens.Profile.screens){
                                popUpTo(0)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text("Profile") }
                    )
                }
            }
        ) {
            NavHost(
                navController = HomeNavController,
                startDestination = Screens.Home.screens,
                modifier = Modifier.padding(it)  // Add padding for content inside Scaffold
            ) {
                composable(Screens.Home.screens) { Home() }
                composable(Screens.Agenda.screens) { Agenda() }
                composable(Screens.Mahasiswa.screens) { MahasiswaMain() }
                composable(Screens.Dosen.screens) { DosenMain() }
                composable(Screens.Profile.screens) { ProfileMain(MainNavController) }
                composable(Screens.Jadwal.screens) { Jadwal() }
            }
        }
    }
}
