package com.example.oladapogiwa

import androidx.room.*


@Entity(tableName = "poi")
data class poiEntity (
    @PrimaryKey(autoGenerate = true)
        val id: Long,
        val name: String,
        val type:String,
        val description:String,
        val latitude : Double,
        val longitude : Double
)