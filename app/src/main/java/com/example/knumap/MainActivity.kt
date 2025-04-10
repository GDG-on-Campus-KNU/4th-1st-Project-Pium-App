package com.example.knumap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val accessToken = prefs.getString("accessToken", null)
        val refreshToken = prefs.getString("refreshToken", null)
        val accountId = prefs.getInt("accountId", -1)
        val userRole = prefs.getString("userRole", null)

        if (accessToken.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        finish() // 현재 액티비티는 종료
    }
}
