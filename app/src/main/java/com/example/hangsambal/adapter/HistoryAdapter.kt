package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.model.response.GetHistoryData

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var history = mutableListOf<GetHistoryData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = history[position]
        holder.textViewNama.text = data.nama?.toUpperCase()
        val splitDateTime = data.dateTrans?.split(" ") ?: listOf("")
        val splitDate = splitDateTime.first().split("-")
        if (splitDate.size == 3) {
            holder.textViewTanggal.text = "${splitDate[2]} ${getMonthName(splitDate[1].toInt())} ${splitDate[0]}"
        }
        holder.textViewJumlahProduk.text = "${data.jmlQtyProduct} Produk"
    }

    override fun getItemCount(): Int = history.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewTanggal: TextView = itemView.findViewById(R.id.textViewTanggal)
        val textViewJumlahProduk: TextView = itemView.findViewById(R.id.textViewJumlahProduk)
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> "Januari"
        }
    }
}