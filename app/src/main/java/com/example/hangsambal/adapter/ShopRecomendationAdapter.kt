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
import com.example.hangsambal.util.ItemClickListener

class ShopRecommendationAdapter(
    private var shops: List<GetShopData>,
    private val listener: ItemClickListener<GetShopData>
) : RecyclerView.Adapter<ShopRecommendationAdapter.ViewHolder>() {

    fun updateData(newShops: List<GetShopData>) {
        this.shops = newShops
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toko_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shops[position]

        Glide.with(holder.itemView.context)
            .load(shop.photoShop)
            .placeholder(R.drawable.ic_no_image)
            .error(R.drawable.ic_no_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageViewToko)

        holder.textViewNamaToko.text = shop.nameShop
        holder.textViewAlamatToko.text = shop.detlocShop

        shop.distanceShop?.toFloatOrNull()?.let { distance ->
            if (distance > 1000) {
                holder.textViewJarakToko.text = "${(distance / 1000).toInt()} km"
            } else {
                holder.textViewJarakToko.text = "${distance.toInt()} m"
            }
            holder.textViewJarakToko.visibility = View.VISIBLE
        } ?: run {
            holder.textViewJarakToko.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { listener.onClickItem(shop) }
    }

    override fun getItemCount() = shops.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewToko: ImageView = itemView.findViewById(R.id.imageViewToko)
        val textViewNamaToko: TextView = itemView.findViewById(R.id.textViewNamaToko)
        val textViewAlamatToko: TextView = itemView.findViewById(R.id.textViewAlamatToko)
        val textViewJarakToko: TextView = itemView.findViewById(R.id.textViewJarakToko)
    }
}