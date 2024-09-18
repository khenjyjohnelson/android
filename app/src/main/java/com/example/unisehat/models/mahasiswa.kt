package com.example.unisehat.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class mahasiswa(
    val userId: String? = null,
    val nim: String? = null,
    val nama: String? = null,
    val email: String? = null,
    val jurusan: String? = null,
    val angkatan: Int? = null,
    val password: String? = null
)