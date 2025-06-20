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
import com.example.hangsambal.adapter.BannerAdapter
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

    // === Tambahan untuk Banner Carousel ===
    private lateinit var bannerHandler: Handler
    private lateinit var bannerRunnable: Runnable
    private var currentBannerPage = 0
    private val bannerList = listOf(
        R.drawable.banner_satu,
        R.drawable.banner_dua,
        R.drawable.banner_tiga
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val data = JWTUtils.decoded(Prefs(requireContext()).jwt.toString())
        binding.textViewNama.text = data.nameUser.toString()

        // === Inisialisasi Banner ViewPager ===
        binding.bannerViewPager.adapter = BannerAdapter(bannerList)

        bannerHandler = Handler(Looper.getMainLooper())
        bannerRunnable = object : Runnable {
            override fun run() {
                currentBannerPage = (currentBannerPage + 1) % bannerList.size
                binding.bannerViewPager.setCurrentItem(currentBannerPage, true)
                bannerHandler.postDelayed(this, 5000)
            }
        }

        // === Lokasi ===
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }
        locationSettingsRequestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let {
                    getGeocoder(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Tidak dapat mendapatkan lokasi, silahkan coba lagi", Toast.LENGTH_SHORT).show()
                }
            }
        }
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        checkSelfPermission()

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(requireContext(), "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
        }

        viewModel.getDashboardV2(requireContext())
        viewModel.getCekPickup(requireContext())

        binding.recyclerViewListStock.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = productAdapter
            val marginDecoration = PixelHelper.convertDpToPx(4, resources)
            addItemDecoration(HorizontalSpaceDecoration(marginDecoration, 4))
        }

        binding.recyclerViewListToko.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = shopAdapter
            val marginDecoration = PixelHelper.convertDpToPx(4, resources)
            addItemDecoration(HorizontalSpaceDecoration(marginDecoration, 4))
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
                    binding.textViewMessagePickup.text = it
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
                    binding.textViewMessagePickup.text = it
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showAlertDialog(it)
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

            Handler().postDelayed({ binding.swipeRefreshLayout.isRefreshing = false }, 3000)
        }

        return binding.root
    }

    private fun showAlertDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Pesan")
            .setMessage(message)
            .setPositiveButton(android.R.string.yes) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getShopList(page: Int) {
        if (activity != null && latitude.isNotEmpty() && longitude.isNotEmpty()) {
            viewModel.getShopRecommendation(requireContext(), latitude, longitude, page)
        }
    }

    private fun checkSelfPermission() {
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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), locationPermissionCode)
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkSelfPermission()
        } else {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                Toast.makeText(requireContext(), "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
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

    override fun onLocationChanged(location: Location) {
        getGeocoder(location)
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

        // Start auto-scroll banner
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }

    override fun onPause() {
        binding.shimmerFrameLayoutListToko.stopShimmer()
        binding.shimmerFrameLayoutStock.stopShimmer()

        // Stop auto-scroll banner
        bannerHandler.removeCallbacks(bannerRunnable)

        super.onPause()
    }
}
