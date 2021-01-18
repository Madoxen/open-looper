package com.example.openlooper.repository

import androidx.lifecycle.LiveData
import com.example.openlooper.data.FavoriteDao
import com.example.openlooper.model.Favorite
import java.util.*


class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    val readAll: LiveData<List<Favorite>> = favoriteDao.readAll()

    suspend fun addFavorite(fav: Favorite){
        favoriteDao.add(fav)
    }

    suspend fun updateFavorite(fav: Favorite){
        favoriteDao.update(fav)
    }

    suspend fun deleteFavorite(fav: Favorite){
        favoriteDao.delete(fav)
    }

}