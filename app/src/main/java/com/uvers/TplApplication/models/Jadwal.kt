package com.uvers.TplApplication.models

data class Jadwal(
    var number: String? = "",
    val mataKuliah: String = "",
    val hari: String = "",
    val waktuMulai: String = "",
    val waktuSelesai: String = "",
    val semester: String = "",
    val type: String = "",
    val uid: String = "",
    val kodeMk: String = "",
    val ruangan: String = "",
){
    constructor():this("", "", "", "", "", "","","")
}
