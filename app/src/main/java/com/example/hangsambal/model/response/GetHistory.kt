package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetHistory(
    @SerializedName("data")
    val dataHistory: List<GetHistoryData>?,
    @SerializedName("status_pagination")
    val statusPagination: List<StatusPagination>?
) : Parcelable, Base()

@Parcelize
data class GetHistoryData(
    @SerializedName("ID_TRANS")
    val idTrans: String?,
    @SerializedName("USERNAME_USER")
    val usernameUser: String?,
    @SerializedName("DATE_TRANS")
    val dateTrans: String?,
    @SerializedName("JML_QTY_PRODUCT")
    val jmlQtyProduct: String?,
    @SerializedName("NAMA_TOKO", alternate = ["NAMA_PASAR", "NAMA_AREA"])
    val nama: String?
) : Parcelable
