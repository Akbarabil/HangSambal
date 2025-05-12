package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class Base(
    @SerializedName("status_code")
    val statusCode: Int? = 0,
    @SerializedName("status_message")
    val statusMessage: String? = null,
) : Parcelable