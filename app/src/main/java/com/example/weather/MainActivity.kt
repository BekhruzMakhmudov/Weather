package com.example.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import okio.IOException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var marker: Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map=googleMap
        map.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            val currentLatLng = LatLng(it.latitude, it.longitude)
            marker = map.addMarker(MarkerOptions().position(currentLatLng).title("Tashkent"))!!
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            getWeatherData(it.latitude, it.longitude)
        }
        map.setOnMapClickListener { latLng ->
            marker.position = latLng
            getWeatherData(latLng.latitude,latLng.longitude)
        }
    }
    private fun getWeatherData(lat: Double, lon: Double) {
        val client = OkHttpClient()
        val apiKey = "1819c3473c246da2a2c8bb2d480ce0ee"
        val urlString = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"
//        val city = "Tashkent"
//        val urlString = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
        val request = Request.Builder()
            .url(urlString)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody!!)
                val main = jsonResponse.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val weatherDescription = jsonResponse.getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("description")
                runOnUiThread {
                    marker.title = "$temperature°C, $weatherDescription"
                    marker.showInfoWindow()
                }
            }
        })
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}