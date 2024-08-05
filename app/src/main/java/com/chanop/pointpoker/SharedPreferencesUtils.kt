package com.chanop.pointpoker
import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {
    private const val PREFERENCES_NAME = "app_point_poker" // Choose a suitable name
    const val userID = "user_id"
    const val userName = "user_name"

    // Function to get SharedPreferences instance
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    // Convenient functions to save different data types
    fun putString(context: Context, key: String, value: String) {
        getSharedPreferences(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context,key: String): String {
        return getSharedPreferences(context).getString(key,"") ?: ""
    }

    fun clearAll(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}
