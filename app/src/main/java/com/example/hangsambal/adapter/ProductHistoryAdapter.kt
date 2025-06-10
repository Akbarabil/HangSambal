package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.model.response.GetProductHistoryData

class ProductHistoryAdapter : RecyclerView.Adapter<ProductHistoryAdapter.ViewHolder>() {
    var products: List<GetProductHistoryData> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk_ambil, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewNamaProduk.text = products[holder.adapterPosition].nameProduct + " (pax)"
        holder.editTextJumlahProduk.setText(products[holder.adapterPosition].qtyTd)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewNamaProduk = ItemView.findViewById<View>(R.id.textViewNamaProduk) as TextView
        val editTextJumlahProduk = ItemView.findViewById<View>(R.id.editTextJumlahProduk) as EditText
    }
}