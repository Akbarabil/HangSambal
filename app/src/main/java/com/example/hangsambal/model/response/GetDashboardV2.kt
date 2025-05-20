package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetDashboardV2(
    @SerializedName("data")
    val dataDashboardV2: List<GetDashboardV2Data>?
) : Parcelable, Base()

@Parcelize
data class GetDashboardV2Data(
    @SerializedName("SPREADNIG")
    val spreading: String?,
    @SerializedName("UB_UBLP")
    val ubUblp: String?,
    @SerializedName("DAYS")
    val days: String?,
    @SerializedName("OFF_TARGET")
    val offTarget: String?,
    @SerializedName("PROGRESS")
    val progress: String?,
    @SerializedName("DATA_CATEGORY")
    val dataCategory: List<List<GetCategoryData>>?
) : Parcelable

@Parcelize
data class GetCategoryData(
    @SerializedName("NAME")
    val name: String?,
    @SerializedName("AVG")
    val avg: String?,
    @SerializedName("PROG_DETAIL")
    val progDetail: String?,
    @SerializedName("REAL")
    val real: String?,
    @SerializedName("TGT")
    val tgt: String?
) : Parcelable