package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetCekPickupProduk(
    @SerializedName("status_success")
    val statusSuccess: Int?
) : Parcelable, Base()