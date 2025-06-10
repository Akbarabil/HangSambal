package com.example.hangsambal.view.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.R
import com.example.hangsambal.adapter.ImageHistoryAdapter
import com.example.hangsambal.adapter.ProductHistoryAdapter
import com.example.hangsambal.databinding.ActivityDetailVisitBinding
import com.example.hangsambal.util.JWTUtils
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.viewmodel.DetailVisitViewModel
import java.text.SimpleDateFormat
import java.util.Date

class DetailVisitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailVisitBinding
    private lateinit var viewModel: DetailVisitViewModel
    private var productAdapter: ProductHistoryAdapter = ProductHistoryAdapter()
    private var imageAdapter: ImageHistoryAdapter = ImageHistoryAdapter()

    private var idTrans = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailVisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idTrans = intent.getStringExtra(KeyIntent.KEY_ID_TRANSACTION).toString()

        viewModel = ViewModelProvider(this).get(DetailVisitViewModel::class.java)

        binding.recyclerViewProduk.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProduk.adapter = productAdapter

        binding.recyclerViewDokumentasi.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDokumentasi.adapter = imageAdapter

        viewModel.getDetailVisit(this, idTrans)
        viewModel.detail.observe(this) {
            if (it != null) {
                binding.textViewNama.text = it.nameShop ?: "-"
                val date = it.dateTrans.toString().split(" ").first().split("-")
                binding.textViewTanggal.text = "${date[2]} ${getMonthName(date[1].toInt())} ${date[0]}"
                binding.textViewAlamat.text = it.detailAlamat

                if (!it.productTerjual.isNullOrEmpty()) {
                    productAdapter.products = it.productTerjual
                    productAdapter.notifyDataSetChanged()
                }

                imageAdapter.idType = it.idType.toString()

                if (it.image != null) {
                    if (!it.image.url.isNullOrEmpty()) {
                        imageAdapter.imageList = it.image.url
                    }
                    if (!it.image.descImage.isNullOrEmpty()) {
                        imageAdapter.descList = it.image.descImage
                    }
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

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