package com.example.unisehat.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Daftar(
    val userId: String? = null,
    val jadwalId: String? = null,
)
