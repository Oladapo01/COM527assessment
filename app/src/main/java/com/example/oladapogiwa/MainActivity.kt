package com.example.oladapogiwa


import android.content.pm.PackageManager
import android.location.Location
import android.Manifest
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.httpGet
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import com.github.kittinunf.fuel.gson.responseObject // for GSON - uncomment when needed
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem


//import com.example.oladapogiwa.AddPointOfInterestActivity.PointOfInterest


class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val pointOfInterestList = mutableListOf<PointOfInterest>()
    private var marker: Marker? = null
    private lateinit var db: poiDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        mapView = findViewById(R.id.map)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        db = poiDatabase.getDatabase(applicationContext)
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 225)
            return
        }
        // Location listener
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Get the device's current location
                val geoPoint = GeoPoint(location.latitude, location.longitude)

                // Center the map on the device's current location
                mapView.controller.setCenter(geoPoint)
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        // Get the device's current location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

        // Center the map on the device's current location
        val geoPoint = GeoPoint(0.0, 0.0)
        mapView.controller.setCenter(geoPoint)

        // Set the map zoom level
        mapView.controller.setZoom(14.0)


        //Add map click event listener to add a marker
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
               p?.let {point ->
                   val intent = Intent(this@MainActivity, AddPointOfInterestActivity::class.java)
                   intent.putExtra("latitude", point.latitude)
                   intent.putExtra("longitude", point.longitude)
                   startActivityForResult(intent, Companion.ADD_POINT_REQUEST_CODE)
                   // Add marker to map when clicked on
                   val marker = Marker(mapView)
                   marker.position = p
                   marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                   mapView.overlays.add(marker)
                   return true
               } ?: run {
                   return false
                   //mapView.invalidate()
               }

            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(eventsOverlay)

        if(savedInstanceState != null){
            // Restore the map state
            val latitude = savedInstanceState.getDouble("latitude")
            val longitude = savedInstanceState.getDouble("longitude")
            if(latitude != 0.0 && longitude != 0.0){
                val geoPoint = GeoPoint(latitude, longitude)
                marker = Marker(mapView)
                mapView.controller.animateTo(geoPoint)

                //Restore the marker state
                val markerLatitude = savedInstanceState.getDouble("markerLatitude")
                val markerLongitude = savedInstanceState.getDouble("markerLongitude")
                if(markerLatitude != 0.0 && markerLongitude != 0.0) {
                    marker = Marker(mapView)
                    marker?.position = GeoPoint(markerLatitude, markerLongitude)
                    mapView.overlays.add(marker)
                }

            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
            // Restore  the map state
            val latitude = savedInstanceState.getDouble("latitude")
            val longitude = savedInstanceState.getDouble("longitude")
            if(latitude != 0.0 && longitude != 0.0){
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.animateTo(geoPoint)

                //Restore the marker state
                val markerLatitude = savedInstanceState.getDouble("markerLatitude")
                val markerLongitude = savedInstanceState.getDouble("markerLongitude")
                if(markerLatitude != 0.0 && markerLongitude != 0.0) {
                    marker = Marker(mapView)
                    marker?.position = GeoPoint(markerLatitude, markerLongitude)
                    mapView.overlays.add(marker)
                }

            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 225) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    // Check whether locationListener has been initialized before accessing it
                    if (::locationListener.isInitialized) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                    }
                   // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                }
            } else {
                // Permission denied
                Log.e("MainActivity", "Permission denied")
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_POINT_REQUEST_CODE && resultCode == RESULT_OK) {
            val name = data?.getStringExtra("name")
            val type = data?.getStringExtra("type")
            val description = data?.getStringExtra("description")
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            if (name != null && type != null && description != null && latitude != null && longitude != null) {
                val newPOI = PointOfInterest(name, type, description, longitude, latitude)
                pointOfInterestList.add(newPOI)
            } else {
                Log.e("MainActivity", "Received null data from AddPointOfInterestActivity")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addPoint -> {
                val intent = Intent(this, AddPointOfInterestActivity::class.java)
                startActivityForResult(intent, Companion.ADD_POINT_REQUEST_CODE)
                return true
            }
        }
        when (item.itemId) {
            R.id.savePoint -> {
                val db = poiDatabase.getDatabase(applicationContext)

                for (pointOfInterest in pointOfInterestList){
                    val name = pointOfInterest.name
                    val type = pointOfInterest.type
                    val desc = pointOfInterest.description
                    val lat = pointOfInterest.latitude
                    val lon = pointOfInterest.longitude
                    Log.d("MainActivity", "Inserting POI: $name, $type, $desc, $lat, $lon")
                    lifecycleScope.launch {
                        //var insertId = 0L
                        withContext(Dispatchers.IO) {
                            val savedpoi = poiEntity(0,name,type,desc,lat,lon)
                            Log.d("MainActivity", "Inserting POI: $savedpoi")
                            val insertId = db.poiDao().insert(savedpoi)
                            Log.d("MainActivity", "POI saved with id $insertId")
                        }
                    }
                }
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                pointOfInterestList.clear() // clearing it after that
                return true
            }
        }
        when (item.itemId) {
            R.id.preference -> {
                val intent = Intent(this, Preferences::class.java)
                startActivity(intent)
                return true
            }
        }
        when (item.itemId) {
            R.id.displaypoi ->{
            val db = poiDatabase.getDatabase(applicationContext)
                lifecycleScope.launch {
                    val pois = withContext(Dispatchers.IO) {
                        db.poiDao().getAll()
                    }
                    Log.d("MainActivity", "POIs: $pois")
                    pointOfInterestList.clear()
                    mapView.overlays.clear()
                    for (poi in pois) {
                        val pointOfInterest = PointOfInterest(
                            poi.name,
                            poi.type,
                            poi.description,
                            poi.longitude,
                            poi.latitude
                        )
                        pointOfInterestList.add(pointOfInterest)
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(poi.latitude, poi.longitude)
                        marker.title = poi.name
                        mapView.overlays.add(marker)
                    }
                    // Center the map on the first POI
                    if (pointOfInterestList.isNotEmpty()) {
                        val geoPoint = GeoPoint(pointOfInterestList[0].latitude, pointOfInterestList[0].longitude)
                        mapView.controller.animateTo(geoPoint)
                    }
                    // Redraw the map with the new markers
                    mapView.invalidate()
                }
                return true
            }
        }
        when(item.itemId) {
            R.id.displayWebPOI -> {
                mapView.overlays.clear()
                val webPOI = mutableListOf<Marker>()
                if (webPOI.isEmpty()) {
                    val url = "http://10.0.2.2:3000/poi/all"
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            url.httpGet().responseObject<List<poiList>> { _, _, result ->
                                when (result) {
                                    is Result.Success -> {
                                        val poilist = result.get()
                                        Log.d("MainActivity", "POIs: $poilist")

                                        for (poi in poilist) {
                                            val marker = Marker(mapView)
                                            marker.position = GeoPoint(poi.lat, poi.lon)
                                            Log.d("MainActivity", "Lat: ${poi.lat}" + " Lon: ${poi.lon}")
                                            marker.title = poi.name
                                            mapView.overlays.add(marker)
                                            webPOI.add(marker)
                                        }
                                        mapView.invalidate()
                                    }
                                    is Result.Failure -> {
                                        Log.e("MainActivity", "Failed to fetch POIs: ${result.error}")
                                    }
                                    else -> {
                                        Log.e("MainActivity", "An unknown error occurred")
                                        mapView.invalidate()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    mapView.overlays.addAll(webPOI)
                    mapView.invalidate()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    companion object {
        private const val ADD_POINT_REQUEST_CODE = 1
    }
    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val upload = sharedPreferences.getBoolean("uploadPOI", false) ?: true
        if (upload){
            val url = "http://10.0.2.2:3000/poi/create"
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    val pois = db.poiDao().getAll()
                        for (poi in pois) {
                            val poiUpload= listOf(
                                "name" to poi.name,
                                "type" to poi.type,
                                "description" to poi.description,
                                "lon" to poi.longitude,
                                "lat" to poi.latitude
                                )
                            url.httpPost(poiUpload).response { _, response, result ->
                                when (result) {
                                    is Result.Success -> {
                                        val data = result.get()
                                        Log.d("MainActivity", "Data: ${data}")
                                    }
                                    is Result.Failure -> {
                                        Log.e("MainActivity", "Error: ${result.error}")

                                    }

                                }
                            }
                        }
                    }
                }
            Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
        }
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //db.close()
    }
}



data class PointOfInterest(val name: String, val type: String, val description: String, val longitude: Double, val latitude: Double) {
    override fun toString(): String {
   return "$name, $type, $description"
    }
}
