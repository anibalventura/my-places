package com.anibalventura.myplaces.data.repository

import androidx.lifecycle.LiveData
import com.anibalventura.myplaces.data.db.PlaceDao
import com.anibalventura.myplaces.data.model.PlaceModel

class PlaceRepository(private val placeDao: PlaceDao) {

    val getDatabase: LiveData<List<PlaceModel>> = placeDao.getDatabase()

    suspend fun insertItem(placeModel: PlaceModel) {
        placeDao.insertItem(placeModel)
    }

    suspend fun updateItem(placeModel: PlaceModel) {
        placeDao.updateItem(placeModel)
    }

    suspend fun deleteItem(placeModel: PlaceModel) {
        placeDao.deleteItem(placeModel)
    }

    suspend fun deleteDatabase() {
        placeDao.deleteDatabase()
    }
}