package com.example.openlooper.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.openlooper.converter.GeoPointListConverter
import org.osmdroid.util.GeoPoint

@Entity(tableName = "favorite_table")
@TypeConverters(GeoPointListConverter::class)
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val distance: Double,
    val coordinates: List<GeoPoint>,
    val MaxAltitude: Double = 0.0
)


