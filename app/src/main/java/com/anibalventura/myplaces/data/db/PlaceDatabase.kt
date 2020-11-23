package com.anibalventura.myplaces.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.anibalventura.myplaces.data.model.PlaceModel

@Database(entities = [PlaceModel::class], version = 1, exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        fun getDatabase(context: Context): PlaceDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java,
                    "place_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}