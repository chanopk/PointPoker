package com.chanop.pointpoker.model

data class RoomsModel(
    val roomList: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class RoomModel(
    val room: Room? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Room(
    val id: String,
    val name: String,
    val leader: String,
    val averagePoint: Double? = null,
    val points: List<Double> = listOf(),
    val memberCount: Int? = null,
    val owner: Boolean = false,
    val recent: Boolean = false,
)