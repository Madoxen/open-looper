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
import com.example.openlooper.utils.GeographicalUtils.Companion.calculateGeographicalDistance
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class RouteVM : ViewModel() {
    private val logging: HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val httpClient: OkHttpClient.Builder = OkHttpClient.Builder().addInterceptor(logging)
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create()) //This converts from java objects to JSON and vice versa
        .baseUrl("https://api.openrouteservice.org/")
        .client(httpClient.build())
        .build()

    private val service: ORSService = retrofit.create(ORSService::class.java)
    val currentRoute: LiveData<List<GeoPoint>> = MutableLiveData<List<GeoPoint>>()
    val deltaHeight: LiveData<Double> = MutableLiveData<Double>(0.0); //EGZAMIN
    val distance: MutableLiveData<Int> = MutableLiveData(1000);
    var lastPoint: GeoPoint? = null
    val random: Random = Random()


    init {
        clearRoute()
    }

    fun getNewRoute(point: GeoPoint = GeoPoint(51.0, 0.0)) {
        //Launch coroutine
        viewModelScope.launch {
            try {
                val s = service.getRoute(
                    "foot-hiking", BuildConfig.ORS_API,
                    GETRoundRoute.BuildFromGeoPoint(
                        point,
                        Options(
                            Round_trip(
                                distance.value!!,
                                random.nextInt()
                            )
                        )
                    )
                )
                if (currentRoute is MutableLiveData<*>) {
                    val l: MutableList<GeoPoint> = mutableListOf()
                    var maxHeight = Double.MIN_VALUE;
                    var minHeight = Double.MAX_VALUE;

                    //EGZAMIN
                    //=====================================================
                    s.features[0].geometry.coordinates.forEach {
                        l.add(GeoPoint(it[1], it[0]))

                        if(it.count() >= 3)
                        {
                            if(maxHeight < it[2])
                                maxHeight = it[2];

                            if(minHeight > it[2])
                                minHeight = it[2]
                        }
                    }

                    if(deltaHeight is MutableLiveData<Double>)
                    {
                        if(maxHeight != Double.MIN_VALUE && minHeight != Double.MAX_VALUE)
                        {
                            deltaHeight.value = maxHeight - minHeight;
                        }
                        else
                        {
                            deltaHeight.value = 0.0;
                        }
                    }
                    //======================================================
                    currentRoute.value = l
                }
            } catch (e: Exception) {
                Log.e("OpenLooper", e.message.toString())
            }
        }
    }

    fun setRoute(points: List<GeoPoint>) {
        if (currentRoute is MutableLiveData<*>)
            currentRoute.value = points.toMutableList()
    }

    fun clearRoute() {
        if (currentRoute is MutableLiveData<*>)
            currentRoute.value = mutableListOf<GeoPoint>()
    }

    //Adds point to currentRoute
    fun addPoint(point: GeoPoint) {
        if (currentRoute is MutableLiveData<*>) {
            //This is wasteful, because we are throwing entire list away instead of modifying already existing one.
            val c = currentRoute.value as List<GeoPoint>
            val l: MutableList<GeoPoint> = c.toMutableList()
            l.add(point)
            currentRoute.value = l

            if(deltaHeight is MutableLiveData<Double>)//EGZAMIN
                deltaHeight.value = 0.0;//EGZAMIN
        }
    }

    fun getRouteTotalLength(): Double {
        var dist = 0.0
        currentRoute.value?.let {
            val limit = it.count() - 2 //omit last value
            if (limit < 1) return dist
            for (i in 0..limit) {
                dist += calculateGeographicalDistance(it[i], it[i + 1])
            }
        }
        return dist
    }

}