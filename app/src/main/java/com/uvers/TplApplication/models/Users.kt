package com.uvers.TplApplication.models

data class Users(
    val identifier: String,
    val nomorInduk: String = "",
    val nama: String,
    val angkatan: String = "",
    val jurusan: String = "Teknik Perangkat Lunak",
    val semester: String = "",
    val email: String = "",
    val instagramUsername: String = "",
    val facebookUsername: String = "",
    val linkedln: String = "",
    val youtubeName: String = "",
    val whatsapp: String = "",
    val mbti: String = "",
    var photoUrl: String = "",
    val password: String,
    val judulTa: String = "",
    val dospem1: String = "",
    val dospem2: String = "",
    val tempatKerja: String = "",
    val jabatanPekerjaan: String = "",
    val ketLulus: String = "",
    val role: String = ""
){
    constructor() : this("", "","","", "", "", "", "", "", "", "", "", "", "", "", "", "","","","","","")
}
