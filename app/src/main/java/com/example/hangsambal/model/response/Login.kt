package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class LogIn(
    @SerializedName("data")
    val dataLogIn: LogInData?
) : Parcelable, Base()

@Parcelize
data class LogInData(
    @SerializedName("jwt")
    val jwt: String?
) : Parcelable
