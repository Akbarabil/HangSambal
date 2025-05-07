package com.example.hangsambal.model.request

data class OptimizationRequest(
    val coordinates: List<List<Double>>,  // Daftar koordinat (longitude, latitude)
    val profile: String,                  // Profil perjalanan (driving, walking, cycling)
    val source: Int,                      // Index dari titik awal
    val destination: Int                  // Index dari titik akhir
)