package com.example.hangsambal.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityLoginBinding
import com.example.hangsambal.viewmodel.LoginViewModel
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reset agar tutorial selalu muncul saat testing
        MaterialShowcaseView.resetSingleUse(this, "SHOWCASE_LOGIN")

        binding.root.post {
            showStepByStepTutorials()
        }
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.materialButtonLogin.setOnClickListener {
            InternetUtils.checkInternetBeforeAction(this) {
                if ((binding.textInputEditTextUsername.text.isNullOrEmpty() || binding.textInputEditTextUsername.text.isNullOrBlank()) && (binding.textInputEditTextPassword.text.isNullOrEmpty() || binding.textInputEditTextPassword.text.isNullOrBlank())) {
                    binding.textInputEditTextUsername.error = "Mohon isi username"
                    binding.textInputEditTextPassword.error = "Mohon isi password"
                } else if (binding.textInputEditTextUsername.text.isNullOrEmpty() || binding.textInputEditTextUsername.text.isNullOrBlank()) {
                    binding.textInputEditTextUsername.error = "Mohon isi username"
                } else if (binding.textInputEditTextPassword.text.isNullOrEmpty() || binding.textInputEditTextPassword.text.isNullOrBlank()) {
                    binding.textInputEditTextPassword.error = "Mohon isi password"
                } else {
                    binding.materialButtonLogin.isEnabled = false
                    viewModel.signIn(
                        baseContext,
                        binding.textInputEditTextUsername.text.toString(),
                        binding.textInputEditTextPassword.text.toString()
                    )
                }
            }
        }

        binding.materialButtonLogin.setOnClickListener {
            val intent = Intent(this, PresenceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showStepByStepTutorials() {
        val config = ShowcaseConfig()
        config.delay = 500

        val sequence = MaterialShowcaseSequence(this, "SHOWCASE_LOGIN")
        sequence.setConfig(config)

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this).setTarget(binding.textInputEditTextUsername)
                .setTitleText("Username").setDismissText("Oke")
                .setContentText("Masukkan data username anda").withRectangleShape(false)
                .setShapePadding(8).build()
        )

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this).setTarget(binding.textInputEditTextPassword)
                .setTitleText("Password").setDismissText("Oke")
                .setContentText("Masukkan password Anda dengan benar").withRectangleShape(false)
                .setShapePadding(8).build()
        )

        sequence.start()
    }
}