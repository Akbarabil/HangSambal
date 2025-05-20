package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetPickup(
    @SerializedName("data")
    val dataPickup: GetPickupData?
) : Parcelable, Base()

@Parcelize
data class GetPickupData(
    @SerializedName("id_pickup")
    val idPickup: String?,
    @SerializedName("product")
    val products: List<GetPickupProductData>?
) : Parcelable

@Parcelize
data class GetPickupProductData(
    @SerializedName("id_product")
    val idProduct: String?,
    @SerializedName("name_product")
    val nameProduct: String?,
    @SerializedName("image_product")
    val imageProduct: String?,
    @SerializedName("qty_product")
    val qtyProduct: Int?
) : Parcelable