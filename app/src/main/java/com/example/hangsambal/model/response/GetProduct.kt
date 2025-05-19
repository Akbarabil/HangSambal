package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetProduct(
    @SerializedName("data")
    val dataProduct: List<GetProductData>?
) : Parcelable, Base()

@Parcelize
data class GetProductData(
    @SerializedName("ID_PRODUCT")
    val idProduct: String?,
    @SerializedName("ID_PC")
    val idPc: String?,
    @SerializedName("NAME_PRODUCT")
    val nameProduct: String?,
    @SerializedName("deleted_at")
    val deletedAt: String?,
    @SerializedName("CODE_PRODUCT")
    val codeProduct: String?
) : Parcelable