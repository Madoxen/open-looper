package com.example.openlooper.VM

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openlooper.BuildConfig
import com.example.openlooper.model.GETRoundRoute
import com.example.openlooper.model.ORSService
import com.example.openlooper.model.RoundRoute.Options
import com.example.openlooper.model.RoundRoute.Round_trip
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class RouteVM : ViewModel() {
    private val logging: HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private val httpClient: OkHttpClient.Builder = OkHttpClient.Builder().addInterceptor(logging);
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create()) //This converts from java objects to JSON and vice versa
        .baseUrl("https://api.openrouteservice.org/")
        .client(httpClient.build())
        .build()

    private val service: ORSService = retrofit.create(ORSService::class.java)
    val currentRoute: LiveData<List<GeoPoint>> = MutableLiveData<List<GeoPoint>>();
    var lastPoint: GeoPoint? = null;
    val random: Random = Random();


    init {
        clearRoute();
    }

    fun getNewRoute(point: GeoPoint = GeoPoint(51.0, 0.0)) {
        //Launch coroutine
        viewModelScope.launch {
            try {
                val s = service.getRoute(
                    "foot-walking", BuildConfig.ORS_API,
                    GETRoundRoute.BuildFromGeoPoint(point, Options(Round_trip(10000, random.nextInt())))
                )
                if (currentRoute is MutableLiveData<List<GeoPoint>>) {
                    val l: MutableList<GeoPoint> = ArrayList();
                    s.features[0].geometry.coordinates.forEach {
                        l.add(GeoPoint(it[1], it[0]))
                    }
                    currentRoute.value = l;
                }
            } catch (e: Exception) {
                Log.e("OpenLooper", e.message.toString());
            }
        }
    }

    fun setRoute(points: List<GeoPoint>)
    {
        if (currentRoute is MutableLiveData<List<GeoPoint>>)
            currentRoute.value = points;
    }

    fun clearRoute()
    {
        if (currentRoute is MutableLiveData<List<GeoPoint>>)
            currentRoute.value = listOf();
    }

    //Adds point to currentRoute
    fun addPoint(point: GeoPoint) {
        if (currentRoute is MutableLiveData<List<GeoPoint>>) {
            //This is wasteful, because we are throwing entire list away instead of modifying already existing one.
            val l: MutableList<GeoPoint> = currentRoute.value?.toMutableList() ?: mutableListOf();
            l.add(point);
            currentRoute.value = l;
        }
    }

}