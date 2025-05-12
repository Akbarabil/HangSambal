package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R

class TokoAdapter(private val tokoList: List<TokoModel>) :
    RecyclerView.Adapter<TokoAdapter.TokoViewHolder>() {

    inner class TokoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaToko: TextView = itemView.findViewById(R.id.textViewNamaToko)
        val alamatToko: TextView = itemView.findViewById(R.id.textViewAlamatToko)
        val jarakToko: TextView = itemView.findViewById(R.id.textViewJarakToko)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TokoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return TokoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TokoViewHolder, position: Int) {
        val toko = tokoList[position]
        holder.namaToko.text = toko.nama
        holder.alamatToko.text = toko.alamat
        holder.jarakToko.text = toko.jarak
    }

    override fun getItemCount(): Int = tokoList.size
}