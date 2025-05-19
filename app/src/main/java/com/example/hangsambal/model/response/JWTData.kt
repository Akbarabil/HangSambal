package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class JWTData(
    @SerializedName("USERNAME_USER")
    val usernameUser: String? = null,

    @SerializedName("NAME_USER")
    val nameUser: String? = null,

    @SerializedName("KTP_USER")
    val ktpUser: String? = null,

    @SerializedName("TELP_USER")
    val telpUser: String? = null,

    @SerializedName("EMAIL_USER")
    val emailUser: String? = null
) : Parcelable