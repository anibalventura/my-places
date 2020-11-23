package com.anibalventura.myplaces.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.anibalventura.myplaces.data.db.PlaceDatabase
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.data.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val placeDao = PlaceDatabase.getDatabase(application).placeDao()
    private val repository: PlaceRepository

    val getDatabase: LiveData<List<PlaceModel>>

    init {
        repository = PlaceRepository(placeDao)
        getDatabase = repository.getDatabase
    }

    fun insertItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertItem(placeModel)
        }
    }

    fun updateItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(placeModel)
        }
    }

    fun deleteItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(placeModel)
        }
    }

    fun deleteDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDatabase()
        }
    }
}