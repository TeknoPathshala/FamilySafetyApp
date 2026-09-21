package com.example.familysafety

import android.content.Context

object AppConfig {
    private const val PREF = "family_safety"
    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    fun apiKey(c: Context) = p(c).getString("apiKey", "") ?: ""
    fun projectId(c: Context) = p(c).getString("projectId", "") ?: ""
    fun email(c: Context) = p(c).getString("email", "") ?: ""
    fun uid(c: Context) = p(c).getString("uid", "") ?: ""
    fun idToken(c: Context) = p(c).getString("idToken", "") ?: ""
    fun familyCode(c: Context) = p(c).getString("familyCode", "") ?: ""
    fun name(c: Context) = p(c).getString("name", "") ?: ""
    fun sharing(c: Context) = p(c).getBoolean("sharing", false)
    fun saveFirebase(c: Context, key: String, project: String) = p(c).edit().putString("apiKey", key).putString("projectId", project).apply()
    fun saveAuth(c: Context, email: String, uid: String, token: String) = p(c).edit().putString("email", email).putString("uid", uid).putString("idToken", token).apply()
    fun saveProfile(c: Context, name: String, code: String) = p(c).edit().putString("name", name).putString("familyCode", code).apply()
    fun setSharing(c: Context, on: Boolean) = p(c).edit().putBoolean("sharing", on).apply()
    fun clearAuth(c: Context) = p(c).edit().clear().apply()
}
