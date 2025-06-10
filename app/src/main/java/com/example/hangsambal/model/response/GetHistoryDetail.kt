package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetHistoryDetail(
    @SerializedName("data")
    val dataHistoryDetail: GetHistoryDetailData?
) : Parcelable, Base()

@Parcelize
data class GetHistoryDetailData(
    @SerializedName("ID_TRANS")
    val idTrans: String?,
    @SerializedName("ID_TYPE")
    val idType: String?,
    @SerializedName("USERNAME_USER")
    val usernameUser: String?,
    @SerializedName("DATE_TRANS")
    val dateTrans: String?,
    @SerializedName("PRODUCT_TERJUAL")
    val productTerjual: List<GetProductHistoryData>?,
    @SerializedName("IMAGE")
    val image: GetImageHistoryData?,
    @SerializedName("NAME_SHOP")
    val nameShop: String?,
    @SerializedName("DETAIL_ALAMAT")
    val detailAlamat: String?
) : Parcelable

@Parcelize
data class GetProductHistoryData(
    @SerializedName("NAME_PRODUCT")
    val nameProduct: String?,
    @SerializedName("QTY_TD")
    val qtyTd: String?
) : Parcelable

@Parcelize
data class GetImageHistoryData(
    @SerializedName("URL")
    val url: List<String>?,
    @SerializedName("DESC_IMAGE")
    val descImage: List<String>?
) : Parcelable