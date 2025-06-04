package com.example.hangsambal.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.model.request.PostProductTransaction
import com.example.hangsambal.model.response.GetPickupProductData
import com.example.hangsambal.model.response.GetProductData

class SpreadingAdapter : RecyclerView.Adapter<SpreadingAdapter.ViewHolder>() {
    var products: List<GetProductData> = emptyList()
    var productsPickup: List<GetPickupProductData> = emptyList()
    var productsTransaction: List<PostProductTransaction> = emptyList()
    var isUb = false
    var isTrans = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewNamaProduk.text = products[position].nameProduct + " (pax)"

        val pickupQty = productsPickup[position].qtyProduct.toString().toIntOrNull() ?: 0

        if (pickupQty > 0) {
            // Set default jumlah sesuai qty pickup
            holder.editTextJumlahProduk.setText(pickupQty.toString())
            productsTransaction[position].qtyProduct = pickupQty

            holder.editTextJumlahProduk.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val inputQty = holder.editTextJumlahProduk.text.toString().toIntOrNull() ?: 0
                    if (inputQty > pickupQty) {
                        holder.editTextJumlahProduk.setText(pickupQty.toString())
                        productsTransaction[position].qtyProduct = pickupQty
                        holder.imageViewPlus.visibility = View.INVISIBLE
                    } else {
                        productsTransaction[position].qtyProduct = inputQty
                        holder.imageViewPlus.visibility = View.VISIBLE
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            holder.imageViewMinus.setOnClickListener {
                var jumlah = holder.editTextJumlahProduk.text.toString().toIntOrNull() ?: 0
                if (jumlah > 0) {
                    jumlah--
                    holder.editTextJumlahProduk.setText(jumlah.toString())
                    productsTransaction[position].qtyProduct = jumlah
                    holder.imageViewPlus.visibility = View.VISIBLE
                }
            }

            holder.imageViewPlus.setOnClickListener {
                var jumlah = holder.editTextJumlahProduk.text.toString().toIntOrNull() ?: 0
                if (jumlah < pickupQty) {
                    jumlah++
                    holder.editTextJumlahProduk.setText(jumlah.toString())
                    productsTransaction[position].qtyProduct = jumlah

                    if (jumlah == pickupQty) {
                        holder.imageViewPlus.visibility = View.INVISIBLE
                    }
                }
            }

            holder.editTextJumlahProduk.isEnabled = isTrans
            holder.imageViewMinus.visibility = if (isTrans) View.VISIBLE else View.INVISIBLE
            holder.imageViewPlus.visibility = if (isTrans) View.VISIBLE else View.INVISIBLE
        } else {
            holder.editTextJumlahProduk.setText("0")
            productsTransaction[position].qtyProduct = -1
            holder.editTextJumlahProduk.isEnabled = false
            holder.imageViewMinus.visibility = View.INVISIBLE
            holder.imageViewPlus.visibility = View.INVISIBLE
        }
    }


    override fun getItemCount(): Int {
        return products.size
    }

    fun sumProductTransaction(): Int {
        var total = 0
        for (product in productsTransaction) {
            if (product.qtyProduct.toString().toInt() >= 0) {
                total += product.qtyProduct.toString().toInt()
            }
        }
        return total
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewNamaProduk = ItemView.findViewById<View>(R.id.textViewNamaProduk) as TextView
        val editTextJumlahProduk = ItemView.findViewById<View>(R.id.editTextJumlahProduk) as EditText
        val imageViewMinus = ItemView.findViewById<View>(R.id.imageViewMinus) as ImageView
        val imageViewPlus = ItemView.findViewById<View>(R.id.imageViewPlus) as ImageView
    }
}