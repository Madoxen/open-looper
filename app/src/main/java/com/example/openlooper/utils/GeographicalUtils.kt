package com.example.openlooper.utils

import org.osmdroid.util.GeoPoint
import kotlin.math.*

class GeographicalUtils {

    companion object {
        //Since our app is working only on earth, we take earths radius
        private const val EARTH_RADIUS =
            6357.0 //radius ranges from 6357km to 6358km, this is in [m]

        //Calculates approximate geographical distance
        fun calculateGeographicalDistance(A: GeoPoint, B: GeoPoint): Double {

            val a = GeoPoint(Math.toRadians(A.latitude), Math.toRadians(A.longitude))
            val b = GeoPoint(Math.toRadians(B.latitude), Math.toRadians(B.longitude))

            val deltaLat = abs(a.latitude - b.latitude)
            val deltaLong = abs(a.longitude - b.longitude)

            //https://en.wikipedia.org/wiki/Great-circle_distance
            val angle = sqrt(
                (cos(b.latitude) * sin(deltaLong)).pow(2.0) + (cos(a.latitude) * sin(b.latitude) - sin(
                    a.latitude
                ) * cos(b.latitude) * cos(deltaLong)).pow(2.0)
            ) / (sin(a.latitude) * sin(b.latitude) + cos(a.latitude) * cos(b.latitude) * cos(
                deltaLong
            ))

            return atan(angle) * EARTH_RADIUS
        }


    }
}