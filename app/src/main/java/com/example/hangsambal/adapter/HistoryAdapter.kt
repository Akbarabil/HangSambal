package com.example.hangsambal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.model.response.GetHistoryData
import com.example.hangsambal.util.ItemClickListener

class HistoryAdapter(private var listener: ItemClickListener<GetHistoryData>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    var history = mutableListOf<GetHistoryData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewNama.text = history[position].nama.toString().toUpperCase()
        val splitDateTime = history[position].dateTrans.toString().split(" ")
        val splitDate = splitDateTime.first().split("-")
        holder.textViewTanggal.text = "${splitDate[2]} ${getMonthName(splitDate[1].toInt())} ${splitDate[0]}"
        holder.textViewJumlahProduk.text = history[position].jmlQtyProduct.toString() + " Produk"

        holder.itemView.setOnClickListener {
            listener.onClickItem(history[position])
        }
    }

    override fun getItemCount(): Int {
        return history.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewNama = ItemView.findViewById<View>(R.id.textViewNama) as TextView
        val textViewTanggal = ItemView.findViewById<View>(R.id.textViewTanggal) as TextView
        val textViewJumlahProduk = ItemView.findViewById<View>(R.id.textViewJumlahProduk) as TextView
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