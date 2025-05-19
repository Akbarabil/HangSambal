package com.example.hangsambal.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hangsambal.databinding.FragmentProfileBinding
import com.example.hangsambal.util.JWTUtils
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.view.activity.LoginActivity

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val data = JWTUtils.decoded(Prefs(requireContext()).jwt.toString())
        binding.textViewNama.text = data.nameUser.toString()
        binding.textViewNamaLengkap.text = data.nameUser.toString()
        binding.textViewKTP.text = data.ktpUser.toString()
        binding.textViewHP.text = data.telpUser.toString()
        binding.textViewEmail.text = data.emailUser.toString()

        binding.materialButtonKeluar.setOnClickListener {
            Prefs(requireContext()).jwt = null
            Prefs(requireContext()).idDistrict = null
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        return binding.root
    }
}