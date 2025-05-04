package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.view.activity.ProductPickupActivity

class ProductPickupAdapter : RecyclerView.Adapter<ProductPickupAdapter.ViewHolder>() {

    var products: List<ProductPickupActivity.PostProductPickup> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNamaProduk: TextView = itemView.findViewById(R.id.textViewNamaProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_pick_up, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.textViewNamaProduk.text = product.name
    }

    override fun getItemCount(): Int = products.size
}