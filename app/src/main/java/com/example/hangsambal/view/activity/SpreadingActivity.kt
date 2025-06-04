package com.example.hangsambal.view.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.R
import com.example.hangsambal.adapter.SpreadingAdapter
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivitySpreadingBinding
import com.example.hangsambal.model.request.PostProductTransaction
import com.example.hangsambal.model.request.PostTransaction
import com.example.hangsambal.util.JWTUtils
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.SpreadingViewModel
import java.text.SimpleDateFormat
import java.util.Date

class SpreadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpreadingBinding
    private lateinit var viewModel: SpreadingViewModel
    private lateinit var dialog : ProgressDialog
    private var adapter = SpreadingAdapter()
    private var idShop: String = ""
    private var isTrans: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpreadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)

        idShop = intent.getStringExtra(KeyIntent.KEY_ID_SHOP).toString()

        viewModel= ViewModelProvider(this).get(SpreadingViewModel::class.java)

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

        binding.recyclerViewProduk.setHasFixedSize(true)
        binding.recyclerViewProduk.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProduk.adapter = adapter

        binding.checkBoxIsTrans.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                isTrans = 0
                adapter.isTrans = false
                adapter.notifyDataSetChanged()
            } else {
                isTrans = 1
                adapter.isTrans = true
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.getCekPickup(this)
        viewModel.products.observe(this) {
            adapter.products = it
            val data = mutableListOf<PostProductTransaction>()
            it.forEach {
                data.add(PostProductTransaction(it.idProduct, it.idPc, 0))
            }
            adapter.productsTransaction = data
            adapter.notifyDataSetChanged()
        }

        viewModel.productsPickup.observe(this) {
            adapter.productsPickup = it
            adapter.notifyDataSetChanged()
        }

        viewModel.stateCekPickup.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }
                State.LOADING -> {
                    showProgressDialog("Sedang memeriksa data pickup...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.statePickupProduct.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }
                State.LOADING -> {
                    showProgressDialog("Sedang mengambil data produk pickup...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.stateProduct.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }
                State.LOADING -> {
                    showProgressDialog("Sedang mengambil data produk...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), false)
            }
        }

        viewModel.errorMessageCek.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), true)
            }
        }


        binding.materialButtonSelanjutnya.setOnClickListener {
            InternetUtils.checkInternetBeforeAction(this) {
                if (isTrans == 1) {
                    if (adapter.sumProductTransaction() > 0) {
                        val data = adapter.productsTransaction.toMutableList()
                            .filter { it.qtyProduct.toString().toInt() > 0 }.toMutableList()

                        val postTransaction = PostTransaction(
                            idShop,
                            "1",
                            data,
                            adapter.sumProductTransaction(),
                            0,
                            isTrans,
                            "",
                            ""
                        )
                        val intent = Intent(this, DocumentationActivity::class.java)
                        intent.putExtra(KeyIntent.KEY_TRANSACTION, postTransaction)
                        startActivity(intent)
                    } else {
                        showAlertDialog("Jumlah produk tidak boleh kosong", false)
                    }
                } else {
                    val data = mutableListOf<PostProductTransaction>()
                    adapter.productsTransaction.forEach {
                        if (it.qtyProduct.toString().toInt() >= 0) {
                            val dataTrans = it
                            dataTrans.qtyProduct = 0
                            data.add(dataTrans)
                        }
                    }

                    val postTransaction = PostTransaction(
                        idShop,
                        "1",
                        data,
                        adapter.sumProductTransaction(),
                        0,
                        isTrans
                    )
                    val intent = Intent(this, DocumentationActivity::class.java)
                    intent.putExtra(KeyIntent.KEY_TRANSACTION, postTransaction)
                    startActivity(intent)
                }
            }
        }
    }

    private fun showAlertDialog(message: String, isFinish: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
            if (isFinish) {
                finish()
            }
        }
        builder.show()
    }

    private fun showProgressDialog(message: String) {
        //show dialog
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.show()
    }
}