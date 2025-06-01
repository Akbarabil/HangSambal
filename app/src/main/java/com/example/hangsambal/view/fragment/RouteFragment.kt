package com.example.hangsambal.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.R
import com.example.hangsambal.adapter.TokoAdapter
import com.example.hangsambal.adapter.TokoModel
import com.example.hangsambal.databinding.FragmentRouteBinding
import com.example.hangsambal.view.activity.MerchantDataActivity
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
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class RouteFragment : Fragment() {

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationPlugin: LocationComponentPlugin
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentPoint: Point? = null
    private lateinit var currentStyle: Style
    private var isFirstRequest = true
    private var hasCenteredCamera = false

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

        binding.mapViewRoute.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
            currentStyle = style
            initLocationComponent()
            initAnnotationManager()
            requestLocationPermission()
        }

        binding.refreshItem.setOnClickListener {
            isFirstRequest = true
            runOptimization()
        }

        binding.tambahTokoButton.setOnClickListener{
            val intent = Intent(requireContext(), MerchantDataActivity::class.java)
            startActivity(intent)
        }

        val dummyList = listOf(
            TokoModel("Idim Store", "Jl. Retribution Gg v", "2 km"),
            TokoModel("Toko Gojo", "Jl. Ulti Sampah Lord", "1.5 km"),
            TokoModel("Toko C", "Jl. C No.3", "4.3 km"),
            TokoModel("Toko D", "Jl. D No.4", "5.6 km"),
            TokoModel("Toko E", "Jl. E No.5", "3.1 km")
        )

        binding.recyclerViewListToko.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewListToko.adapter = TokoAdapter(dummyList)
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
        val annotationPlugin = binding.mapViewRoute.annotations
        pointAnnotationManager = annotationPlugin.createPointAnnotationManager()
    }

    private fun enableUserLocation() {
        locationPlugin.addOnIndicatorPositionChangedListener { point ->
            currentPoint = point
            // Hanya pusatkan kamera sekali saat pertama kali lokasi didapat
            if (!hasCenteredCamera) {
                binding.mapViewRoute.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(14.0)
                        .build()
                )
                hasCenteredCamera = true
            }
            if (isFirstRequest) {
                runOptimization()
            }
        }
    }

    private fun generateDummyPoints(): List<Point> {
        return listOf(
            Point.fromLngLat(112.608865, -7.965903),
            Point.fromLngLat(112.608138, -7.968654),
            Point.fromLngLat(112.605828, -7.966417),
            Point.fromLngLat(112.602761, -7.964446)
        )
    }

    private fun runOptimization() {
        Toast.makeText(requireContext(), "Mengirim permintaan optimasi...", Toast.LENGTH_SHORT)
            .show()
        val origin = currentPoint ?: return
        val waypoints = mutableListOf<Point>().apply {
            add(origin)
            addAll(generateDummyPoints())
        }

        val client = MapboxOptimization.builder()
            .coordinates(waypoints)
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
                    val route = response.body()!!.trips()!![0]
                    drawRoute(route.geometry()!!)

                    // ✅ Menampilkan total jarak dan durasi
                    val distanceKm = (route.distance() ?: 0.0) / 1000
                    val jarak = String.format(Locale.US, "%.2f", distanceKm)

                    binding.distanceTextView.text = "Jarak: $jarak km"
                    isFirstRequest = false // Reset flag setelah request selesai
                } else {
                    Log.e("Mapbox", "Optimisasi gagal: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<OptimizationResponse>, t: Throwable) {
                Log.e("Mapbox", "Error: ${t.localizedMessage}")
            }
        })
    }

    private fun drawRoute(geometry: String) {
        val coordinates = LineString.fromPolyline(geometry, 6).coordinates()
        if (coordinates.isEmpty()) return

        val sourceId = "optimized-route-source"
        val layerId = "optimized-route-layer"
        val routePoints = mutableListOf<Point>()

        currentStyle.removeStyleLayer(layerId)
        currentStyle.removeStyleSource(sourceId)

        val animatedSource = geoJsonSource(sourceId) {
            geometry(LineString.fromLngLats(routePoints))
        }
        currentStyle.addSource(animatedSource)

        val lineLayer = lineLayer(layerId, sourceId) {
            lineColor(Color.BLUE)
            lineWidth(5.0)
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
            lineOpacity(0.9)
        }
        currentStyle.addLayer(lineLayer)

        val handler = Handler(Looper.getMainLooper())
        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                if (index < coordinates.size) {
                    routePoints.add(coordinates[index])
                    currentStyle.getSourceAs<GeoJsonSource>(sourceId)?.geometry(
                        LineString.fromLngLats(routePoints)
                    )
                    index++
                    handler.postDelayed(this, 700)
                } else {
                    // Animasi selesai
                    Toast.makeText(requireContext(), "Optimisasi berhasil", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        handler.post(runnable)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}








