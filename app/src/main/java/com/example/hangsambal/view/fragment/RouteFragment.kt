package com.example.hangsambal.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.R
import com.example.hangsambal.adapter.ShopRecommendationAdapter
import com.example.hangsambal.databinding.FragmentRouteBinding
import com.example.hangsambal.model.response.GetShopData
import com.example.hangsambal.util.ItemClickListener
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.view.activity.MerchantDataActivity
import com.example.hangsambal.view.activity.SpreadingActivity
import com.example.hangsambal.viewmodel.RouteViewModel
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.optimization.v1.MapboxOptimization
import com.mapbox.api.optimization.v1.models.OptimizationResponse
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RouteFragment : Fragment() {

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationPlugin: LocationComponentPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentPoint: Point? = null
    private lateinit var currentStyle: Style
    private var hasCenteredCamera = false

    private lateinit var viewModel: RouteViewModel
    private lateinit var adapter: ShopRecommendationAdapter

    private val listener = object : ItemClickListener<GetShopData> {
        override fun onClickItem(item: GetShopData) {
            val distance = item.distanceShop?.toDoubleOrNull() ?: 0.0
            if (distance <= 15) {
                val intent = Intent(requireContext(), SpreadingActivity::class.java)
                intent.putExtra(KeyIntent.KEY_ID_SHOP, item.idShop)
                startActivity(intent)
            } else {
                showAlertDialog("Lokasi anda terlalu jauh dari toko ini", false)
            }
        }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableUserLocation()
            else Toast.makeText(requireContext(), "Izin lokasi diperlukan", Toast.LENGTH_SHORT)
                .show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]

        adapter = ShopRecommendationAdapter(emptyList(), listener)
        binding.recyclerViewListToko.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewListToko.adapter = adapter

        binding.mapViewRoute.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            currentStyle = style
            initLocationComponent()
            initAnnotationManager()
            requestLocationPermission()
        }

        binding.refreshItem.setOnClickListener {
            currentPoint?.let { point ->
                getRecommendedShops(point)
            }
        }

        binding.tambahTokoButton.setOnClickListener {
            val intent = Intent(requireContext(), MerchantDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initLocationComponent() {
        locationPlugin = binding.mapViewRoute.location
        locationPlugin.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun initAnnotationManager() {
        pointAnnotationManager = binding.mapViewRoute.annotations.createPointAnnotationManager()
    }

    private fun enableUserLocation() {
        locationPlugin.addOnIndicatorPositionChangedListener { point ->
            currentPoint = point
            if (!hasCenteredCamera) {
                binding.mapViewRoute.mapboxMap.setCamera(
                    CameraOptions.Builder().center(point).zoom(14.0).build()
                )
                hasCenteredCamera = true
                getRecommendedShops(point)
            }
        }
    }

    private fun getRecommendedShops(userPoint: Point) {
        val lat = userPoint.latitude().toString()
        val lon = userPoint.longitude().toString()

        viewModel.getTop5RecommendedShops(requireContext(), lat, lon) { shops ->
            adapter.updateData(shops)
            runOptimization(listOf(userPoint) + shops.mapNotNull {
                if (!it.latShop.isNullOrEmpty() && !it.longShop.isNullOrEmpty()) {
                    Point.fromLngLat(it.longShop.toDouble(), it.latShop.toDouble())
                } else null
            })
        }
    }

    private fun runOptimization(points: List<Point>) {
        val client = MapboxOptimization.builder()
            .coordinates(points)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.mapbox_access_token))
            .source("first")
            .destination("last")
            .roundTrip(false)
            .build()

        client.enqueueCall(object : Callback<OptimizationResponse> {
            override fun onResponse(
                call: Call<OptimizationResponse>,
                response: Response<OptimizationResponse>
            ) {
                if (response.isSuccessful && response.body()?.trips()?.isNotEmpty() == true) {

                    val trip = response.body()!!.trips()!![0]
                    val geometry = response.body()!!.trips()!![0].geometry()
                    val totalDistanceMeters = trip?.distance() ?: 0.0
                    val totalDistanceKm = totalDistanceMeters / 1000
                    binding.distanceTextView.text = String.format("Total jarak: %.2f km", totalDistanceKm)
                    if (geometry != null) {
                        drawRoute(geometry)
                    }
                }
            }

            override fun onFailure(call: Call<OptimizationResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Gagal optimasi: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun drawRoute(geometry: String) {
        val coordinates = LineString.fromPolyline(geometry, 6).coordinates()
        val sourceId = "route-source"
        val layerId = "route-layer"

        currentStyle.removeStyleLayer(layerId)
        currentStyle.removeStyleSource(sourceId)

        val geoJsonSource = geoJsonSource(sourceId) {
            geometry(LineString.fromLngLats(coordinates))
        }
        currentStyle.addSource(geoJsonSource)

        val lineLayer = lineLayer(layerId, sourceId) {
            lineColor(Color.BLUE)
            lineWidth(5.0)
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
        }
        currentStyle.addLayer(lineLayer)
    }

    private fun showAlertDialog(message: String, success: Boolean) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}








