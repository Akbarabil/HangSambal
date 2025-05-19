package com.example.hangsambal.view.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityLoginBinding
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.LoginViewModel
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var dialog: ProgressDialog
    private var jwt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reset agar tutorial selalu muncul saat testing
        MaterialShowcaseView.resetSingleUse(this, "SHOWCASE_LOGIN")

        binding.root.post {
            showStepByStepTutorials()
        }
        dialog = ProgressDialog(this)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.materialButtonLogin.setOnClickListener {
            // Mengecek koneksi internet sebelum melakukan aksi login
            InternetUtils.checkInternetBeforeAction(this) {

                // Validasi input: username dan password tidak boleh kosong
                if ((binding.textInputEditTextUsername.text.isNullOrEmpty() || binding.textInputEditTextUsername.text.isNullOrBlank()) &&
                    (binding.textInputEditTextPassword.text.isNullOrEmpty() || binding.textInputEditTextPassword.text.isNullOrBlank())
                ) {
                    binding.textInputEditTextUsername.error = "Mohon isi username"
                    binding.textInputEditTextPassword.error = "Mohon isi password"
                } else if (binding.textInputEditTextUsername.text.isNullOrEmpty() || binding.textInputEditTextUsername.text.isNullOrBlank()) {
                    binding.textInputEditTextUsername.error = "Mohon isi username"
                } else if (binding.textInputEditTextPassword.text.isNullOrEmpty() || binding.textInputEditTextPassword.text.isNullOrBlank()) {
                    binding.textInputEditTextPassword.error = "Mohon isi password"
                } else {
                    // Menonaktifkan tombol login saat proses sedang berlangsung
                    binding.materialButtonLogin.isEnabled = false

                    // Memanggil fungsi signIn pada ViewModel
                    viewModel.signIn(
                        baseContext,
                        binding.textInputEditTextUsername.text.toString(),
                        binding.textInputEditTextPassword.text.toString()
                    )
                }
            }
        }

        viewModel.alreadyPresence.observe(this) {
            // Jika login sukses
            if (viewModel.isSuccessSignIn.value == true) {
                if (it) {
                    // Jika user sudah presensi, langsung ke MainActivity
                    startActivity(Intent(baseContext, MainActivity::class.java))
                    finish()
                } else {
                    // Jika belum presensi, diarahkan ke PresenceActivity dengan mengirimkan JWT
                    val intent = Intent(baseContext, PresenceActivity::class.java)
                    intent.putExtra(KeyIntent.KEY_JWT, jwt)
                    startActivity(intent)
                    finish()
                }
            }
        }

        viewModel.stateLogin.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    // Jika login selesai, sembunyikan dialog
                    dialog.dismiss()
                    binding.materialButtonLogin.isEnabled = false
                }

                State.LOADING -> {
                    // Tampilkan dialog saat sedang login
                    showProgressDialog()
                    binding.materialButtonLogin.isEnabled = false
                }

                else -> {
                    // Untuk status lainnya, sembunyikan dialog dan aktifkan tombol login
                    dialog.dismiss()
                    binding.materialButtonLogin.isEnabled = true
                }
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString())
            }
        }

        // Menyimpan token JWT setelah login berhasil
        viewModel.jwt.observe(this) {
            jwt = it.toString()
        }
    }

    // Menampilkan dialog loading
    private fun showProgressDialog() {
        dialog.setMessage("Mohon tunggu...")
        dialog.setCancelable(false)
        dialog.show()
    }

    // Menampilkan alert dialog saat terjadi error
    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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