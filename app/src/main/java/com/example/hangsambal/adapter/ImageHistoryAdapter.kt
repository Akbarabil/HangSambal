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

class ImageHistoryAdapter : RecyclerView.Adapter<ImageHistoryAdapter.ViewHolder>() {
    var imageList : List<String> = emptyList()
    var descList : List<String> = emptyList()
    var idType = "0"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_documentation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (idType.equals("1")) {
            if (position == 0) {
                holder.textViewDokumentasi.text = "Foto Display"
            } else {
                holder.textViewDokumentasi.text = "Foto Lapak"
            }
        } else {
            if (!descList[position].isNullOrEmpty()) {
                holder.textViewDokumentasi.text = "Foto " + descList[position].split(" ").last().toString()
            }
        }

        Glide.with(holder.itemView.context)
            .load(imageList[position])
            .placeholder(R.drawable.ic_no_image)
            .error(R.drawable.ic_no_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.imageViewDokumentasi)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewDokumentasi = ItemView.findViewById<View>(R.id.textViewDokumentasi) as TextView
        val imageViewDokumentasi = ItemView.findViewById<View>(R.id.imageViewDokumentasi) as ImageView
    }
}