package com.example.oladapogiwa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.views.MapView


class AddPointOfInterestActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var addPOIbtnSave: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        Log.d("Add poi from poi", "latitude: $latitude, longitude: $longitude")
        setContentView(R.layout.activity_add_point_of_interest)
        val button = findViewById<Button>(R.id.addPOIbtnSave)

        button.setOnClickListener {
            val names = findViewById<EditText>(R.id.nameEditText).text.toString()
            val types = findViewById<EditText>(R.id.typeEditText).text.toString()
            val descriptions= findViewById<EditText>(R.id.descriptionEditText).text.toString()

            if(names.isEmpty() || types.isEmpty() || descriptions.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.putExtra("name", names)
            Log.d("result", "$names")
            resultIntent.putExtra("type", types)
            Log.d("result", "$types")
            resultIntent.putExtra("description", descriptions)
            Log.d("result", "$descriptions")
            resultIntent.putExtra("latitude", latitude)
            Log.d("result", "$latitude")
            resultIntent.putExtra("longitude", longitude)
            Log.d("result", "$longitude")
            Log.d("result", "$resultIntent")
            setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Point of interest added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}