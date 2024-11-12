package com.uvers.unisehat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unisehat.R
import com.google.firebase.FirebaseApp
import com.uvers.unisehat.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(this)
                }
            }
        }
    }
}

@Composable
fun MainScreen( activity: ComponentActivity) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Splash") {
        composable("Splash") { SplashScreen(navController) }
        composable("Login") { LoginScreen(navController, activity) }
        composable("Home") { HomeScreen(navController) }
//        composable("Test") { TestScreen(navController) }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    // Using LaunchedEffect to delay the navigation to Login screen
    LaunchedEffect(Unit) {
        delay(2000L) // 2 seconds delay
        navController.navigate("Login") {
            popUpTo("Splash") { inclusive = true }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val logoPainter: Painter = painterResource(id = R.drawable.logotpl)
            Image(
                painter = logoPainter,
                contentDescription = "Logo",
                modifier = Modifier.size(230.dp)
            )
        }
    }
}
