package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostPresence(
    @SerializedName("data")
    val dataPostPresence: PostPresenceData?
) : Parcelable, Base()

@Parcelize
data class PostPresenceData(
    @SerializedName("ID_PRESENCE")
    val idPresence: String?
) : Parcelable