package com.example.hangsambal.util

import android.util.Base64
import com.example.hangsambal.model.response.JWTData
import com.google.gson.Gson
import java.io.UnsupportedEncodingException

object JWTUtils {
    fun decoded(JWTEncoded: String) : JWTData {
        val split = JWTEncoded.split("\\.".toRegex()).toTypedArray()
        val gson = Gson()
        val jwtJson = gson.fromJson(getJson(split[1]), JWTData::class.java)
        return jwtJson!!
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset("UTF-8"))
    }
}