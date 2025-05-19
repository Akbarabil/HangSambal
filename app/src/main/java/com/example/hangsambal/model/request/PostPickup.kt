package com.example.hangsambal.model.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostPickup(
    @SerializedName("product")
    var product: List<PostProductPickup>? = null,
    @SerializedName("lat_trans")
    var latTrans: String? = null,
    @SerializedName("long_trans")
    var longTrans: String? = null,
): Parcelable

@Parcelize
data class PostProductPickup(
    @SerializedName("id_product")
    var idProduct: String? = null,
    @SerializedName("name_product")
    var nameProduct: String? = null,
    @SerializedName("total_pickup")
    var totalPickup: Int? = 0
) : Parcelable