package com.example.openlooper.model

import com.example.openlooper.model.RoundRoute.RoundRoute
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ORSService {
    @POST("v2/directions/{profile}/geojson")
    suspend fun getRoute(
        @Path("profile") profile: String,
        @Header("Authorization") Authorization: String,
        @Body body: GETRoundRoute
    ): RoundRoute
}