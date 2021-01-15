package com.example.openlooper.model

import com.example.openlooper.model.RoundRoute.Options
import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint


data class GETRoundRoute(

    @SerializedName("coordinates") var coordinates: List<List<Double>>,
    @SerializedName("options") var options: Options,
    @SerializedName("instructions") var instructions: Boolean = false
) {

    companion object {
        fun BuildFromGeoPoint(
            point: GeoPoint,
            options: Options,
            instructions: Boolean = false
        ): GETRoundRoute {
            //Switch places, because ORS has swiched places of longitude and latitude
            return GETRoundRoute(
                listOf(listOf<Double>(point.longitude, point.latitude)),
                options,
                instructions
            );
        }
    }

}

