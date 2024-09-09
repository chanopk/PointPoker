package com.chanop.pointpoker.intent

import android.content.Context

sealed class RoomIntent {
    data class LoadRoom(val context: Context) : RoomIntent()
    data class JoinRoom(val context: Context, val roomID: String, val name: String) : RoomIntent()
    data class NavigateTo(val path: String): RoomIntent()
    data class RemoveRoom(val roomID: String): RoomIntent()
}