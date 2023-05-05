package com.example.tmpdevelop_d.users

data class Group(
    val groupName: String = "",
    val groupID : String = "",
    val creatorId: String = "",
    val photoUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val totalMembers: Int = 0
) {
    constructor() : this("","", "", "", emptyList(), 0)
}