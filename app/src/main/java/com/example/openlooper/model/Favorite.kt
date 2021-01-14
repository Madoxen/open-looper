package com.example.openlooper.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_table")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val coordinates: String
)

