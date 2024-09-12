package com.chanop.pointpoker.intent

import android.content.Context
import com.chanop.pointpoker.model.RoomModel

sealed class RoomIntent {
    data class LoadRoom(val context: Context, val roomID: String): RoomIntent()
    data class LoadMembers(val context: Context, val roomID: String): RoomIntent()
    data class LeaveRoom(val context: Context, val roomID: String): RoomIntent()
    data class Vote(val context: Context, val roomID: String, val point: Double): RoomIntent()
    data class AveragePoint(val currentRoom: RoomModel): RoomIntent()
    data class ResetAveragePoint(val currentRoom: RoomModel): RoomIntent()
    data class NavigateTo(val path: String): RoomIntent()
    object NavigateBack : RoomIntent()
}