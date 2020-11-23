package com.anibalventura.myplaces.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.anibalventura.myplaces.data.model.PlaceModel

@Dao
interface PlaceDao {

    @Query("SELECT * FROM place_table ORDER BY id DESC")
    fun getDatabase(): LiveData<List<PlaceModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(placeModel: PlaceModel)

    @Update
    suspend fun updateItem(placeModel: PlaceModel)

    @Delete
    suspend fun deleteItem(placeModel: PlaceModel)

    @Query("DELETE FROM place_table")
    suspend fun deleteDatabase()
}