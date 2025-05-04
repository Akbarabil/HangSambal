package com.example.hangsambal.view.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.adapter.ProductPickupAdapter
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityProductPickUpBinding
import com.example.hangsambal.viewmodel.ProductPickupViewModel
import java.util.Calendar

class ProductPickupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductPickUpBinding
    private val adapter = ProductPickupAdapter()
    private val viewModel = ProductPickupViewModel()

    // Dummy lat long
    private var latitude: Double = -6.200000
    private var longitude: Double = 106.816666

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductPickUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.recyclerViewProduk.setHasFixedSize(true)
        binding.recyclerViewProduk.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProduk.adapter = adapter

        // Dummy 5 barang
        val dummyProducts = listOf(
            PostProductPickup(1, "Hang Sambal 20"),
            PostProductPickup(2, "Hang Sambal 30"),
            PostProductPickup(3, "Hang Sambal 40"),
            PostProductPickup(4, "Hang Sambal 100"),
            PostProductPickup(5, "Hang Sambal 200")
        )
        adapter.products = dummyProducts
        adapter.notifyDataSetChanged()

        // Tombol Simpan
        binding.materialButtonSimpan.setOnClickListener {
            InternetUtils.checkInternetBeforeAction(this) {
                val intent = Intent(this, FeedbackActivity::class.java)
                startActivity(intent)
            }
        }
    }

    data class PostProductPickup(
        val id: Int,
        val name: String,
        var qty: Int = 0
    )
}