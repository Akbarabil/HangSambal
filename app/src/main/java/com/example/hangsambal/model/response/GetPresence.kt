package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetPresence(
    @SerializedName("data")
    val dataPresence: List<GetPresenceData>?
) : Parcelable, Base()

@Parcelize
data class GetPresenceData(
    @SerializedName("ID_PRESENCE")
    val idPresence: String?,
    @SerializedName("ID_USER")
    val idUser: String?,
    @SerializedName("ID_TYPE")
    val idType: String?,
    @SerializedName("PHOTO_PRESENCE")
    val photoPresence: String?,
    @SerializedName("DATE_PRESENCE")
    val datePresence: String?,
    @SerializedName("ID_DISTRICT")
    val idDistrict: String?,
    @SerializedName("LONG_PRESENCE")
    val longPresence: String?,
    @SerializedName("LAT_PRESENCE")
    val latPresence: String?
) : Parcelable