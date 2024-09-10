package com.chanop.pointpoker.intent

import android.content.Context

sealed class HomeIntent {
    data class LoadHome(val context: Context) : HomeIntent()
    data class JoinHome(val context: Context, val roomID: String, val name: String) : HomeIntent()
    data class NavigateTo(val path: String): HomeIntent()
    object NavigateBack : HomeIntent()
    data class RemoveHome(val roomID: String): HomeIntent()
}