package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class SignIn(
    @SerializedName("data")
    val dataSignIn: SignInData?
) : Parcelable, Base()

@Parcelize
data class SignInData(
    @SerializedName("jwt")
    val jwt: String?
) : Parcelable
