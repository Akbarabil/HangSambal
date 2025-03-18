package com.example.hangsambal.view.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivityLoginBinding
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aktifkan Edge-to-Edge
        enableEdgeToEdge()

        // Atur padding sesuai system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tambahkan logika tambahan jika diperlukan, misalnya:
        showStepByStepTutorials()

    }
    private fun showStepByStepTutorials() {
        val config = ShowcaseConfig()
        config.delay = 500 // Waktu jeda antar tutorial dalam milidetik

        val sequence = MaterialShowcaseSequence(this, "SHOWCASE_SEQUENCE_ID")
        sequence.setConfig(config)

        // Tutorial untuk tombol pertama
        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.testView1)) // Tombol pertama
                .setTitleText("Gambar Login")
                .setDismissText("Lanjut")
                .setContentText("Menandakan anda berhasil login.")
                .withRectangleShape(true)
                .setShapePadding(8)
                .build()
        )

        sequence.start() // Memulai tutorial bertahap
    }
}