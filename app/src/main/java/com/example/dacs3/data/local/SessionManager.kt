package com.example.dacs3.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.dacs3.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USER = "current_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
