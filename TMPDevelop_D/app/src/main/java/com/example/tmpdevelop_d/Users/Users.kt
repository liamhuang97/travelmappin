package com.example.tmpdevelop_d.Users

data class Users(
    val userID: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val uid: String = "",
) {
    constructor() : this("", "", "")
}