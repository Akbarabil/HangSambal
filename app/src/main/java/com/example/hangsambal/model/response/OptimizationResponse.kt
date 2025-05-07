package com.example.hangsambal.model.response

data class OptimizationResponse(
    val routes: List<Route>  // Daftar rute yang dioptimasi
)

data class Route(
    val geometry: String     // GeoJSON geometry untuk menggambarkan rute
)