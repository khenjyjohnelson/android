package com.uvers.TplApplication.models

data class Pengumuman(
    var id: String? = "",
    val judul: String = "",
    val deskripsi: String = "",
){
    constructor():this("", "", "")
}