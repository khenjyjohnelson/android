package com.uvers.unisehat.control

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

fun mapEnglishDayToIndonesian(englishDay: String): String {
    return when (englishDay.lowercase()) {
        "monday" -> "Senin"
        "tuesday" -> "Selasa"
        "wednesday" -> "Rabu"
        "thursday" -> "Kamis"
        "friday" -> "Jumat"
        "saturday" -> "Sabtu"
        "sunday" -> "Minggu"
        else -> "None" // Default to Monday if no match found
    }
}



@Composable
fun LoadingScreen(isLoading: Boolean) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false, onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
