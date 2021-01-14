package com.example.openlooper.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.openlooper.model.Favorite

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(course: Favorite)

    @Update
    suspend fun update(course: Favorite)

    @Delete
    suspend fun delete(course: Favorite)

    @Query("SELECT * FROM favorite_table ORDER BY id ASC")
    fun readAll(): LiveData<List<Favorite>>


}