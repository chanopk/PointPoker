package com.chanop.pointpoker.intent

import android.content.Context

sealed class RoomIntent {
    data class LoadRoom(val context: Context, val roomID: String): RoomIntent()
    data class LoadMembers(val context: Context, val roomID: String): RoomIntent()
    data class NavigateTo(val path: String): RoomIntent()
    object NavigateBack : RoomIntent()
}