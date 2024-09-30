package com.chanop.pointpoker.intent

import android.content.Context

sealed class CreateRoomIntent {
    data class AddPointList(val point: String) : CreateRoomIntent()
    data class RemovePointList(val point: Double) : CreateRoomIntent()
    data class CreateHome(val context: Context, val roomName: String) : CreateRoomIntent()
    data class NavigateTo(val path: String): CreateRoomIntent()
    object NavigateBack : CreateRoomIntent()
}