package com.example.hangsambal.view.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.R
import com.example.hangsambal.adapter.ShopRecommendationHomeAdapter
import com.example.hangsambal.adapter.StockHomeAdapter
import com.example.hangsambal.databinding.FragmentHomeBinding
import com.example.hangsambal.util.HorizontalSpaceDecoration
import com.example.hangsambal.util.JWTUtils
import com.example.hangsambal.util.PixelHelper
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment(), LocationListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var locationManager: LocationManager
    private var productAdapter: StockHomeAdapter = StockHomeAdapter()
    private var shopAdapter: ShopRecommendationHomeAdapter = ShopRecommendationHomeAdapter()
    private var latitude: String = ""
    private var longitude: String = ""
    private var isFirstLoad = true
    private var page: Int = 1

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var locationCallback: LocationCallback
    val locationPermissionCode = 2

    private var isFakeGPS: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val data = JWTUtils.decoded(Prefs(requireContext()).jwt.toString())
        binding.textViewNama.text = data.nameUser.toString()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000
        locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
        locationSettingsRequestBuilder.addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.lastLocation != null) {
                    getGeocoder(p0.lastLocation!!)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Tidak dapat mendapatkan lokasi, silahkan coba lagi",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
        locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        checkSelfPermission()
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        } else {
            Toast.makeText(requireContext(), "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
        }

        viewModel.getDashboardV2(requireContext())

        viewModel.getCekPickup(requireContext())


        binding.recyclerViewListStock.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productAdapter

            val marginDecoration = PixelHelper.convertDpToPx(4, this.context.resources)
            val itemSpaceDecoration = HorizontalSpaceDecoration(marginDecoration, 4)
            addItemDecoration(itemSpaceDecoration)
        }

        binding.recyclerViewListToko.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = shopAdapter

            val marginDecoration = PixelHelper.convertDpToPx(4, this.context.resources)
            val itemSpaceDecoration = HorizontalSpaceDecoration(marginDecoration, 4)
            addItemDecoration(itemSpaceDecoration)
        }

        viewModel.productsPickup.observe(requireActivity()) {
            binding.shimmerFrameLayoutStock.stopShimmer()
            binding.shimmerFrameLayoutStock.visibility = View.GONE

            if (it.isNullOrEmpty()) {
                binding.linearLayoutEmptyStock.visibility = View.VISIBLE
                binding.recyclerViewListStock.visibility = View.GONE
            } else {
                binding.linearLayoutEmptyStock.visibility = View.GONE
                binding.recyclerViewListStock.visibility = View.VISIBLE
            }

            productAdapter.products = it
            productAdapter.notifyDataSetChanged()
        }

        viewModel.shopList.observe(viewLifecycleOwner) {
            if (page == 1) {
                binding.shimmerFrameLayoutListToko.stopShimmer()
                binding.shimmerFrameLayoutListToko.visibility = View.GONE

                if (it.isNullOrEmpty()) {
                    binding.linearLayoutEmpty.visibility = View.VISIBLE
                    binding.recyclerViewListToko.visibility = View.GONE
                } else {
                    binding.linearLayoutEmpty.visibility = View.GONE
                    binding.recyclerViewListToko.visibility = View.VISIBLE
                }
            }
            shopAdapter.shops.addAll(it?.toMutableList() ?: mutableListOf())
            shopAdapter.notifyItemInserted(shopAdapter.shops.size - 1)
            shopAdapter.notifyDataSetChanged()
        }

        viewModel.stateCekPickup.observe(viewLifecycleOwner) {
            if (it == State.ERROR) {
                binding.shimmerFrameLayoutStock.stopShimmer()
                binding.shimmerFrameLayoutStock.visibility = View.GONE

                binding.linearLayoutEmptyStock.visibility = View.VISIBLE
                binding.recyclerViewListStock.visibility = View.GONE

                viewModel.messagePickup.observe(requireActivity()) {
                    binding.textViewMessagePickup.text = it.toString()
                }
            }
        }

        viewModel.statePickupProduct.observe(viewLifecycleOwner) {
            if (it == State.FINISH_PICKUP) {
                binding.shimmerFrameLayoutStock.stopShimmer()
                binding.shimmerFrameLayoutStock.visibility = View.GONE

                binding.linearLayoutEmptyStock.visibility = View.VISIBLE
                binding.recyclerViewListStock.visibility = View.GONE

                viewModel.messagePickup.observe(requireActivity()) {
                    binding.textViewMessagePickup.text = it.toString()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString())
            }
        }


        binding.cardViewRoute.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                .selectedItemId = R.id.rekomendasi
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            productAdapter.products = emptyList()
            productAdapter.notifyDataSetChanged()

            shopAdapter.shops.clear()
            shopAdapter.notifyDataSetChanged()

            viewModel.getDashboardV2(requireContext())
            viewModel.getCekPickup(requireContext())
            getShopList(page)

            binding.shimmerFrameLayoutStock.startShimmer()
            binding.shimmerFrameLayoutStock.visibility = View.VISIBLE
            binding.linearLayoutEmptyStock.visibility = View.GONE
            binding.recyclerViewListToko.visibility = View.GONE

            binding.shimmerFrameLayoutListToko.startShimmer()
            binding.shimmerFrameLayoutListToko.visibility = View.VISIBLE
            binding.linearLayoutEmpty.visibility = View.GONE
            binding.recyclerViewListToko.visibility = View.GONE

            Handler().postDelayed(
                {
                    binding.swipeRefreshLayout.isRefreshing = false
                }, 3000
            )
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

    private fun getShopList(page: Int) {
        if (activity != null) {
            if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()) {
                viewModel.getShopRecommendation(
                    requireContext(),
                    latitude,
                    longitude,
                    page
                )
            }
        }
    }

    private fun checkSelfPermission() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                locationPermissionCode
            )
        }
    }


    private fun startLocationUpdates() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Izin khusus untuk Android 13+
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                // Izin lama untuk Android 11–12
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            // Meminta izin dengan requireActivity() untuk Fragment
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                locationPermissionCode
            )
        } else {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                // Menggunakan requireContext() untuk Toast di Fragment
                Toast.makeText(requireContext(), "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getGeocoder(location: Location) {
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()

        if (isFirstLoad) {
            getShopList(page)
            isFirstLoad = false
        }
    }

    override fun onLocationChanged(p0: Location) {
        getGeocoder(p0)
    }

    override fun onProviderEnabled(provider: String) {
        Log.e("Location", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.e("Location", "Provider disabled: $provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.e("Location", "onStatusChanged: $provider")
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        binding.shimmerFrameLayoutListToko.startShimmer()
        binding.shimmerFrameLayoutStock.startShimmer()
    }

    override fun onPause() {
        binding.shimmerFrameLayoutListToko.stopShimmer()
        binding.shimmerFrameLayoutStock.stopShimmer()
        super.onPause()
    }
}