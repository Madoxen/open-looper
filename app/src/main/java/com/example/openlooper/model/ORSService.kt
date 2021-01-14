package com.example.openlooper.model

import com.example.openlooper.model.RoundRoute.RoundRoute
import okhttp3.Route
import retrofit2.Call
import retrofit2.http.*

interface ORSService {
    @POST("v2/directions/{profile}/geojson")
    suspend fun getRoute(@Path("profile") profile : String, @Header("Authorization") Authorization : String, @Body body: GETRoundRoute) : RoundRoute;
}