package com.example.openlooper.repository

import androidx.lifecycle.LiveData
import com.example.openlooper.data.FavoriteDao
import com.example.openlooper.model.Favorite
import java.util.*


class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    val readAll: LiveData<List<Favorite>> = favoriteDao.readAll()

    suspend fun addFavorite(grade: Favorite){
        favoriteDao.add(grade)
    }

    suspend fun updateFavorite(grade: Favorite){
        favoriteDao.update(grade)
    }

    suspend fun deleteFavorite(grade: Favorite){
        favoriteDao.delete(grade)
    }

}