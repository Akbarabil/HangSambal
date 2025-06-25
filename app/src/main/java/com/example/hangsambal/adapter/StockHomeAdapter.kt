package com.example.hangsambal.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.hangsambal.R
import com.example.hangsambal.model.response.GetPickupProductData

class StockHomeAdapter() : RecyclerView.Adapter<StockHomeAdapter.ViewHolder>() {
    var products: List<GetPickupProductData> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // Logging untuk melihat isi yang ditampilkan
        Log.d("StockHomeAdapter", "Produk: ${product.nameProduct}, Stok: ${product.qtyProduct}, Posisi: $position")

        Glide.with(holder.itemView.context)
            .load(product.imageProduct)
            .placeholder(R.drawable.ic_no_image)
            .error(R.drawable.ic_no_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageViewProduct)

        holder.textViewNamaProduk.text = product.nameProduct
        holder.textViewStock.text = if (product.qtyProduct!!.toInt() >= 0) {
            "${product.qtyProduct} pax"
        } else {
            "0 pax"
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageViewProduct = ItemView.findViewById<View>(R.id.imageViewProduct) as ImageView
        val textViewNamaProduk = ItemView.findViewById<View>(R.id.textViewNamaProduk) as TextView
        val textViewStock = ItemView.findViewById<View>(R.id.textViewStock) as TextView
    }
}