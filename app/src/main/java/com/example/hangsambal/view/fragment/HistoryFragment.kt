package com.example.hangsambal.view.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hangsambal.R
import com.example.hangsambal.adapter.HistoryAdapter
import com.example.hangsambal.databinding.FragmentHistoryBinding
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HistoryFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private var adapter: HistoryAdapter = HistoryAdapter()
    private lateinit var datePickerDialog: DatePickerDialog

    private var page: Int = 1
    private var tanggal: String = ""
    private var totalPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        datePickerDialog = DatePickerDialog(requireContext(), this, Calendar.getInstance().get(
            Calendar.YEAR), Calendar.getInstance().get(
            Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            datePickerDialog.setOnDateSetListener(this)
        }

        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = adapter

        binding.cardViewTanggal.setOnClickListener {
            datePickerDialog.show()
        }

        binding.cardViewReset.setOnClickListener {
            binding.cardViewReset.visibility = View.GONE
            binding.shimmerFrameLayoutHistory.startShimmer()
            binding.shimmerFrameLayoutHistory.visibility = View.VISIBLE
            binding.linearLayoutContent.visibility = View.GONE

            tanggal = ""
            binding.textViewTanggal.text = "Pilih Tanggal"

            adapter.history.clear()
            adapter.notifyDataSetChanged()

            getHistoryList(page, tanggal)
        }

        viewModel.totalPage.observe(requireActivity()) {
            if (it != null) {
                totalPage = it.toInt()
            }
        }

        binding.recyclerViewHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (page < totalPage) {
                        page++
                        getHistoryList(page, tanggal)
                    }
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.shimmerFrameLayoutHistory.startShimmer()
            binding.shimmerFrameLayoutHistory.visibility = View.VISIBLE
            binding.linearLayoutContent.visibility = View.GONE
            adapter.history.clear()
            adapter.notifyDataSetChanged()

            page = 1
            getHistoryList(page, tanggal)

            Handler().postDelayed(
                {
                    binding.swipeRefreshLayout.isRefreshing = false
                },3000)
        }

        getHistoryList(page, tanggal)
        viewModel.historyList.observe(viewLifecycleOwner) {
            if (page == 1) {
                binding.shimmerFrameLayoutHistory.stopShimmer()
                binding.shimmerFrameLayoutHistory.visibility = View.GONE
                binding.linearLayoutContent.visibility = View.VISIBLE

                if (it.isNullOrEmpty()) {
                    binding.linearLayoutEmpty.visibility = View.VISIBLE
                    binding.linearLayoutData.visibility = View.GONE
                } else {
                    binding.linearLayoutEmpty.visibility = View.GONE
                    binding.linearLayoutData.visibility = View.VISIBLE
                }
            }
            adapter.history.addAll(it?.toMutableList() ?: mutableListOf())
            adapter.notifyItemInserted(adapter.history.size - 1)
            adapter.notifyDataSetChanged()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString())
            }
        }

        return binding.root
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun getHistoryList(page: Int, tanggal: String) {
        viewModel.getHistory(
            requireContext(),
            page,
            tanggal
        )
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            0 -> "Januari"
            1 -> "Februari"
            2 -> "Maret"
            3 -> "April"
            4 -> "Mei"
            5 -> "Juni"
            6 -> "Juli"
            7 -> "Agustus"
            8 -> "September"
            9 -> "Oktober"
            10 -> "November"
            11 -> "Desember"
            else -> "Januari"
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayoutHistory.startShimmer()
    }

    override fun onPause() {
        binding.shimmerFrameLayoutHistory.stopShimmer()
        super.onPause()
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        tanggal = "$p3-${p2 + 1}-$p1"
        val date = "$p3 ${getMonthName(p2)} $p1"
        binding.textViewTanggal.setText(date)

        page = 1
        getHistoryList(page, tanggal)
        adapter.history.clear()
        adapter.notifyDataSetChanged()

        binding.cardViewReset.visibility = View.VISIBLE
        binding.shimmerFrameLayoutHistory.startShimmer()
        binding.shimmerFrameLayoutHistory.visibility = View.VISIBLE
        binding.linearLayoutContent.visibility = View.GONE
    }
}