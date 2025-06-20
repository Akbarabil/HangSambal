package com.example.hangsambal.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.optimization.v1.MapboxOptimization
import com.mapbox.api.optimization.v1.models.OptimizationResponse
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import kotlin.math.abs
import kotlin.math.sqrt

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
    private var originalShops: List<GetShopData> = emptyList()

    private val listener = object : ItemClickListener<GetShopData> {
        override fun onClickItem(item: GetShopData) {
            val intent = Intent(requireContext(), SpreadingActivity::class.java)
            intent.putExtra(KeyIntent.KEY_ID_SHOP, item.idShop)
            startActivity(intent)
        }

        override fun onClickMap(item: GetShopData) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=${item.latShop},${item.longShop}")
            )
            startActivity(intent)
        }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableUserLocation()
            else Toast.makeText(requireContext(), "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
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
        MaterialShowcaseView.resetSingleUse(requireContext(), "SHOWCASE_ROUTE_FRAGMENT")
        binding.root.post {
            showStepByStepTutorials()
        }

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
        pointAnnotationManager = binding.mapViewRoute.annotations.createPointAnnotationManager()
    }

    private fun enableUserLocation() {
        locationPlugin.addOnIndicatorPositionChangedListener { point ->
            currentPoint = point
            if (!hasCenteredCamera) {
                binding.mapViewRoute.mapboxMap.setCamera(
                    CameraOptions.Builder().center(point).zoom(15.0).build()
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
            originalShops = shops
            pointAnnotationManager.deleteAll()
            val routePoints = mutableListOf<Point>()
            routePoints.add(userPoint)

            shops.forEach { shop ->
                if (!shop.latShop.isNullOrEmpty() && !shop.longShop.isNullOrEmpty()) {
                    val shopPoint = Point.fromLngLat(shop.longShop.toDouble(), shop.latShop.toDouble())
                    routePoints.add(shopPoint)
                    createShopMarker(shopPoint, shop.nameShop ?: "Toko")
                }
            }
            val tokoDikirim = shops.mapNotNull { it.nameShop }
            Log.d("RouteDebug", "Toko yang dikirim ke API Optimisasi: $tokoDikirim")
            runOptimization(routePoints)
        }
    }

    private fun createShopMarker(point: Point, name: String) {
        val options = PointAnnotationOptions()
            .withPoint(point)
            .withTextField(name)
            .withTextOffset(listOf(0.0, -2.0))
            .withTextSize(12.0)
            .withTextColor(Color.BLACK)
            .withIconImage("marker-15")

        pointAnnotationManager.create(options)
    }

    private fun findNearestShop(point: Point, shops: List<GetShopData>): GetShopData? {
        return shops.minByOrNull { shop ->
            val shopLat = shop.latShop?.toDoubleOrNull() ?: return@minByOrNull Double.MAX_VALUE
            val shopLng = shop.longShop?.toDoubleOrNull() ?: return@minByOrNull Double.MAX_VALUE
            val dLat = shopLat - point.latitude()
            val dLng = shopLng - point.longitude()
            sqrt(dLat * dLat + dLng * dLng)
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
                    val jsonResponse = Gson().toJson(response.body())
                    Log.d("RouteDebug", "Response dari API Optimization:\n$jsonResponse")
                    val geometry = trip.geometry()
                    val totalDistanceMeters = trip.distance() ?: 0.0
                    val totalDistanceKm = totalDistanceMeters / 1000
                    binding.distanceTextView.text = String.format("Total jarak: %.2f km", totalDistanceKm)

                    if (geometry != null) {
                        val optimizedCoords = LineString.fromPolyline(geometry, 6).coordinates()
                        drawRoute(optimizedCoords)

                        val orderedShops = mutableListOf<GetShopData>()
                        for (point in optimizedCoords) {
                            val matched = findNearestShop(point, originalShops)
                            if (matched != null && !orderedShops.contains(matched)) {
                                orderedShops.add(matched)
                            }
                        }
                        Log.d("RouteDebug", "Urutan toko setelah optimasi:\n${Gson().toJson(orderedShops.map { it.nameShop })}")
                        adapter.updateData(orderedShops)
                    }
                } else {
                    Log.e("RouteDebug", "Response gagal atau kosong: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<OptimizationResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Gagal optimasi: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("RouteDebug", "Optimisasi gagal: ${t.localizedMessage}")
            }
        })
    }

    private fun drawRoute(routePoints: List<Point>) {
        if (routePoints.size < 2) return

        val sourceId = "optimized-route-source"
        val layerId = "optimized-route-layer"
        val arrowLayerId = "arrow-layer"
        val arrowSourceId = "arrow-source"

        currentStyle.removeStyleLayer(layerId)
        currentStyle.removeStyleSource(sourceId)
        currentStyle.removeStyleLayer(arrowLayerId)
        currentStyle.removeStyleSource(arrowSourceId)

        val routeSource = geoJsonSource(sourceId) {
            geometry(LineString.fromLngLats(emptyList()))
        }
        currentStyle.addSource(routeSource)

        val lineLayer = lineLayer(layerId, sourceId) {
            lineColor(ContextCompat.getColor(requireContext(), R.color.green_light))
            lineWidth(5.0)
            lineCap(LineCap.ROUND)
            lineJoin(LineJoin.ROUND)
            lineOpacity(0.9)
        }
        currentStyle.addLayer(lineLayer)

        val arrowSource = geoJsonSource(arrowSourceId) {
            geometry(LineString.fromLngLats(routePoints))
        }
        currentStyle.addSource(arrowSource)

        currentStyle.addImage(
            "arrow-icon",
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_forward)!!.toBitmap()
        )

        val arrowLayer = symbolLayer(arrowLayerId, arrowSourceId) {
            iconImage("arrow-icon")
            symbolPlacement(SymbolPlacement.LINE)
            iconSize(0.5)
            iconRotate(0.0)
            iconAllowOverlap(true)
        }
        currentStyle.addLayerAbove(arrowLayer, layerId)

        val handler = Handler(Looper.getMainLooper())
        val animatedPoints = mutableListOf<Point>()
        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                if (index < routePoints.size) {
                    animatedPoints.add(routePoints[index])
                    currentStyle.getSourceAs<GeoJsonSource>(sourceId)?.geometry(
                        LineString.fromLngLats(animatedPoints)
                    )
                    index++
                    handler.postDelayed(this, 300)
                }
            }
        }
        handler.post(runnable)
    }

    private fun showStepByStepTutorials() {
        val config = ShowcaseConfig().apply {
            delay = 400
        }

        val sequence = MaterialShowcaseSequence(requireActivity(), "SHOWCASE_ROUTE_FRAGMENT").apply {
            setConfig(config)

            addSequenceItem(
                MaterialShowcaseView.Builder(requireActivity())
                    .setTarget(binding.tambahTokoButton)
                    .setTitleText("Tambah Toko")
                    .setDismissText("Selanjutnya")
                    .setContentText("Gunakan ini jika semua toko yang terdaftar sudah habis.")
                    .withRectangleShape(false)
                    .setShapePadding(10)
                    .build()
            )

            addSequenceItem(
                MaterialShowcaseView.Builder(requireActivity())
                    .setTarget(binding.refreshItem)
                    .setTitleText("Refresh Rute")
                    .setDismissText("Selesai")
                    .setContentText("Klik untuk menggambarkan ulang jalur ke toko yang direkomendasikan.")
                    .withRectangleShape(false)
                    .setShapePadding(10)
                    .build()
            )
        }

        sequence.start()
    }

    private fun showAlertDialog(message: String, success: Boolean) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





