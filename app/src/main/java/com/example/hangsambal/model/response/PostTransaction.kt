package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostTransaction(
    @SerializedName("data")
    val dataPostTransaction: PostTransactionData?
) : Parcelable, Base()

@Parcelize
data class PostTransactionData(
    @SerializedName("ID_TRANS")
    val idTrans: String?
) : Parcelable