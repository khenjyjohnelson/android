package com.example.unisehat.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Jadwal(
    val jadwalId: String? = null,
    val tanggal: String? = null,
    val jam: String? = null,
    val hari: String? = null
)
