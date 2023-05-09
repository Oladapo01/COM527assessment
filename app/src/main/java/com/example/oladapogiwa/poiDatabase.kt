package com.example.oladapogiwa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(poiEntity::class), version = 1, exportSchema = false)
abstract class poiDatabase : RoomDatabase(){
    abstract fun poiDao(): poiDAO
    companion object {
        private var instance: poiDatabase? = null
        fun getDatabase(context: Context): poiDatabase {
            var tempInstance = instance
            if(tempInstance == null) {
                tempInstance = Room.databaseBuilder(
                    context.applicationContext,
                    poiDatabase::class.java,
                    "poiDatabase"
                ).build()
                instance = tempInstance
            }
            return tempInstance
        }
    }
}
