package com.chanop.pointpoker.model



data class MembersModel(
    val memberList: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Member(
    val id: String,
    val name: String,
    val point: Double? = null,
    val itsMe: Boolean = false
)