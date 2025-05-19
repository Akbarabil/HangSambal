package com.example.hangsambal.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.model.request.PostProductPickup

class ProductPickupAdapter : RecyclerView.Adapter<ProductPickupAdapter.ViewHolder>() {
    var products: List<PostProductPickup> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_pick_up, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("ADAPTER", "Bind produk: ${products[position].nameProduct}")
        holder.textViewNamaProduk.text = products[holder.adapterPosition].nameProduct + " (pax)"

        holder.checkBoxStockKosong.setOnCheckedChangeListener { _, b ->
            if (b) {
                products[holder.adapterPosition].totalPickup = -1
                holder.editTextJumlahProduk.setText("0")
                holder.editTextJumlahProduk.isEnabled = false
            } else {
                holder.editTextJumlahProduk.isEnabled = true
                products[holder.adapterPosition].totalPickup = 0
            }
        }

        holder.editTextJumlahProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!holder.checkBoxStockKosong.isChecked) {
                    if (holder.editTextJumlahProduk.text.isNullOrEmpty()) {
                        holder.editTextJumlahProduk.setText("0")
                        products[holder.adapterPosition].totalPickup = 0
                    } else {
                        products[holder.adapterPosition].totalPickup = holder.editTextJumlahProduk.text.toString().toInt()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        holder.imageViewMinus.setOnClickListener {
            if (!holder.checkBoxStockKosong.isChecked) {
                var jumlah: Int = holder.editTextJumlahProduk.text.toString().toInt()
                if (jumlah > 0) {
                    jumlah--
                    products[holder.adapterPosition].totalPickup = jumlah
                    holder.editTextJumlahProduk.setText(jumlah.toString())
                }
            }
        }

        holder.imageViewPlus.setOnClickListener {
            if (!holder.checkBoxStockKosong.isChecked) {
                var jumlah: Int = holder.editTextJumlahProduk.text.toString().toInt()
                jumlah++
                products[holder.adapterPosition].totalPickup = jumlah
                holder.editTextJumlahProduk.setText(jumlah.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun sumTotalPickup(): Int {
        var totalPickup = 0
        products.forEach {
            if (it.totalPickup.toString().toInt() > 0) {
                totalPickup += it.totalPickup.toString().toInt()
            }
        }
        return totalPickup
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewNamaProduk = ItemView.findViewById<View>(R.id.textViewNamaProduk) as TextView
        val checkBoxStockKosong = ItemView.findViewById<View>(R.id.checkBoxStockKosong) as CheckBox
        val editTextJumlahProduk = ItemView.findViewById<View>(R.id.editTextJumlahProduk) as EditText
        val imageViewMinus = ItemView.findViewById<View>(R.id.imageViewMinus) as ImageView
        val imageViewPlus = ItemView.findViewById<View>(R.id.imageViewPlus) as ImageView
    }
}