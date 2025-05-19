package com.example.hangsambal.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private val handler = Handler(Looper.getMainLooper()) // Tambahkan handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLottieAnimation()

        binding.materialButtonOke.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }


    private fun startLottieAnimation() {
        binding.lottieConfirm.repeatCount = 0
        binding.lottieConfirm.playAnimation()

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}