package com.example.openlooper.converter

import androidx.room.TypeConverter
import org.osmdroid.util.GeoPoint
import java.util.*

class GeoPointListConverter {

    @TypeConverter
    fun toString(arr: List<GeoPoint>): String {
        var strArr: String = ""
        arr.forEach {

            strArr += it.latitude.toString() + ":"
            strArr += it.longitude.toString() + ":"
            strArr += it.altitude.toString()
            strArr += "|"
        }
        strArr = strArr.dropLast(1)
        //Log.i("WTF",strArr)
        return strArr
    }

    @TypeConverter
    fun fromString(str: String): List<GeoPoint> {
        var array = str.split("|")
        var list: MutableList<GeoPoint> = mutableListOf()
        array.forEach {
            var doubleArr = it.split(":")
            var point =
                GeoPoint(doubleArr[0].toDouble(), doubleArr[1].toDouble(), doubleArr[2].toDouble())
            //Log.i("WTF","${point.latitude} : ${point.longitude} : ${point.altitude}")
            list.add(point)
        }
        return Collections.unmodifiableList(list)
    }

}