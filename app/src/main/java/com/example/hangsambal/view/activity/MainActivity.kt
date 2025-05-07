package com.example.hangsambal.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivityMainBinding
import com.example.hangsambal.view.fragment.HomeFragment
import com.example.hangsambal.view.fragment.MerchantFragment
import com.example.hangsambal.view.fragment.ProfileFragment
import com.example.hangsambal.view.fragment.RouteFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentActivity: Fragment
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_beranda -> {
                fragmentActivity = HomeFragment() // Fragment untuk Beranda
                loadFragment(fragmentActivity)
                return true
            }
            R.id.rekomendasi -> {
                fragmentActivity = RouteFragment() // Fragment untuk Rekomendasi
                loadFragment(fragmentActivity)
                return true
            }
            R.id.tambah_toko -> {
                fragmentActivity = MerchantFragment() // Fragment untuk Profil
                loadFragment(fragmentActivity)
                return true
            }
            R.id.profil -> {
                fragmentActivity = ProfileFragment() // Fragment untuk Profil
                loadFragment(fragmentActivity)
                return true
            }
        }
        return false
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        if (fragment != null) {
            fragmentActivity = fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            return true
        }
        return false
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({doubleBackToExitPressedOnce = false}, 2000)
    }
}


