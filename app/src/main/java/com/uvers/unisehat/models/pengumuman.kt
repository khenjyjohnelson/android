package com.uvers.unisehat.models

data class Pengumuman(
    var id: String? = "",
    val judul: String = "",
    val deskripsi: String = "",
){
    constructor():this("", "", "")
}