package com.example.hangsambal.model.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostTransaction(
    @SerializedName("id_shop")
    var idShop: String? = null,
    @SerializedName("id_type")
    var idType: String? = null,
    @SerializedName("product")
    var product: List<PostProductTransaction>? = null,
    @SerializedName("qty_trans")
    var qtyTrans: Int? = null,
    @SerializedName("total_trans")
    var totalTrans: Int? = 0,
    @SerializedName("is_trans")
    var isTrans: Int? = 1,
    @SerializedName("lat_trans")
    var latTrans: String? = null,
    @SerializedName("long_trans")
    var longTrans: String? = null,
    @SerializedName("fake_status")
    var isFakeGPS: Int? = 0,
): Parcelable

@Parcelize
data class PostProductTransaction(
    @SerializedName("id_product")
    var idProduct: String? = null,
    @SerializedName("id_pc")
    var idPc: String? = null,
    @SerializedName("qty_product")
    var qtyProduct: Int? = 0
) : Parcelable