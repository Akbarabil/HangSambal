package com.example.hangsambal.util

import android.content.Context

class Prefs (context: Context) {
    private val KEY_JWT = "KEY_JWT"
    private val KEY_DISTRICT = "KEY_DISTRICT"

    private val preferencesJWT = context.getSharedPreferences(KEY_JWT, Context.MODE_PRIVATE)
    private val preferencesIdDistrict = context.getSharedPreferences(KEY_DISTRICT, Context.MODE_PRIVATE)

    var jwt: String?
        get() = preferencesJWT.getString(KEY_JWT, null)
        set(value) = preferencesJWT.edit().putString(KEY_JWT, value).apply()

    var idDistrict: String?
        get() = preferencesIdDistrict.getString(KEY_DISTRICT, null)
        set(value) = preferencesIdDistrict.edit().putString(KEY_DISTRICT, value).apply()
}