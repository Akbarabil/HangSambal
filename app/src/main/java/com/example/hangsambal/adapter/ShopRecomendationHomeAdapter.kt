package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.hangsambal.R
import com.example.hangsambal.model.response.GetShopData

class ShopRecommendationHomeAdapter : RecyclerView.Adapter<ShopRecommendationHomeAdapter.ViewHolder>() {
    var shops = mutableListOf<GetShopData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toko_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(shops[position].photoShop)
            .placeholder(R.drawable.ic_no_image)
            .error(R.drawable.ic_no_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageViewToko)

        holder.textViewNamaToko.text = shops[position].nameShop

        var distance = Math.round(shops[position].distanceShop.toString().toFloat())
        if (distance > 1000) {
            distance /= 1000
            holder.textViewJarakToko.text = "$distance km"
        } else {
            holder.textViewJarakToko.text = "$distance m"
        }

        holder.textViewAlamatToko.text = shops[position].detlocShop
    }

    override fun getItemCount(): Int {
        return if (shops.size > 5) 5 else shops.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewToko = itemView.findViewById<ImageView>(R.id.imageViewToko)
        val textViewNamaToko = itemView.findViewById<TextView>(R.id.textViewNamaToko)
        val textViewJarakToko = itemView.findViewById<TextView>(R.id.textViewJarakToko)
        val textViewAlamatToko = itemView.findViewById<TextView>(R.id.textViewAlamatToko)
    }
}
