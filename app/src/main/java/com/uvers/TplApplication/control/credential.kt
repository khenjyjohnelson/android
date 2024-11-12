package com.uvers.TplApplication.control

import android.content.Context

fun getSavedCredentials(context: Context): Pair<String, String>? {
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("identifier", null)
    val role = sharedPreferences.getString("role", null)
    val timestamp = sharedPreferences.getLong("timestamp", 0L)
    val currentTime = System.currentTimeMillis()
    val savedTime = 1000 * 60 * 60 * 2

    // Check if the saved credentials are older than 2 hours (7200000 milliseconds)
    return if (username != null && role != null && (currentTime - timestamp) < savedTime) {

        Pair(username, role)
    } else {
        // If credentials are expired, clear them
        clearCredentials(context)
        null
    }
}

fun clearCredentials(context: Context) {
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        clear()
        apply()
    }
}


fun saveCredentials(context: Context, identifier: String, role: String) {
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val currentTime = System.currentTimeMillis()
    with(sharedPreferences.edit()) {
        putString("identifier", identifier)
        putString("role", role)
        putLong("timestamp", currentTime)
        apply()
    }
}