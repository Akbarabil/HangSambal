package com.example.hangsambal.view.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivityMerchantDataBinding

class MerchantDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMerchantDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantDataBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}