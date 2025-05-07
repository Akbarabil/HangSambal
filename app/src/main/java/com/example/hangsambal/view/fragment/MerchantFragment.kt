package com.example.hangsambal.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hangsambal.R
import com.example.hangsambal.databinding.FragmentMerchantBinding
import com.example.hangsambal.view.activity.MerchantDataActivity

class MerchantFragment : Fragment() {
    private lateinit var binding: FragmentMerchantBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.materialButtonTambahToko.setOnClickListener {
            val intent = Intent(requireContext(), MerchantDataActivity::class.java)
            startActivity(intent)
        }
    }
}