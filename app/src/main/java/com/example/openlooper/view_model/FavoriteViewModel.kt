package com.example.openlooper.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.openlooper.data.AppDatabase
import com.example.openlooper.model.Favorite
import com.example.openlooper.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application): AndroidViewModel(application) {

    val readAllFavorite: LiveData<List<Favorite>>
    private val repository: FavoriteRepository

    init {
        val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)
        readAllFavorite = repository.readAll
    }

    fun addCourse(favorite: Favorite){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavorite(favorite)
        }
    }

    fun updateCourse(favorite: Favorite){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavorite(favorite)
        }
    }

    fun deleteCourse(favorite: Favorite){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavorite(favorite)
        }
    }
}