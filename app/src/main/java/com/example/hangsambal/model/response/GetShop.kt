package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetShop(
    @SerializedName("data")
    val dataShop: List<GetShopData>?,
    @SerializedName("status_pagination")
    val statusPagination: List<StatusPagination>?
) : Parcelable, Base()

@Parcelize
data class GetShopData(
    @SerializedName("ID_SHOP")
    val idShop: String?,
    @SerializedName("PHOTO_SHOP")
    val photoShop: String?,
    @SerializedName("NAME_SHOP")
    val nameShop: String?,
    @SerializedName("OWNER_SHOP")
    val ownerShop: String?,
    @SerializedName("DISTANCE_SHOP")
    val distanceShop: String?,
    @SerializedName("DETLOC_SHOP")
    val detlocShop: String?,
    @SerializedName("LAT_SHOP")
    val latShop: String?,
    @SerializedName("LONG_SHOP")
    val longShop: String?,
    @SerializedName("LASTTRANS_SHOP")
    val lastTrans: String?
) : Parcelable

@Parcelize
data class StatusPagination(
    @SerializedName("TOTAL_DATA")
    val totalData: Int?,
    @SerializedName("PAGE")
    val page: Int?,
    @SerializedName("TOTAL_PAGE")
    val totalPage: Int?
) : Parcelable