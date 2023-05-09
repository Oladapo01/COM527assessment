package com.example.oladapogiwa

import android.util.Log
import androidx.room.*

@Dao
interface poiDAO {
    @Query("SELECT * FROM poi")
    fun getAll(): List<poiEntity>

    @Insert
    suspend fun insert(poi: poiEntity): Long
}
