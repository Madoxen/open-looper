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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log


class RouteVM : ViewModel() {
    private val logging : HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private val httpClient : OkHttpClient.Builder = OkHttpClient.Builder().addInterceptor(logging);
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create()) //This converts from java objects to JSON and vice versa
        .baseUrl("https://api.openrouteservice.org/")
        .client(httpClient.build())
        .build()

    private val service: ORSService = retrofit.create(ORSService::class.java)
    val currentRoute: LiveData<List<GeoPoint>> = MutableLiveData<List<GeoPoint>>();

    init {
        getRoute();
    }

    fun getRoute()
    {
        viewModelScope.launch {
            try {
                val s = service.getRoute("foot-walking", BuildConfig.ORS_API,
                    GETRoundRoute(listOf(listOf<Double>(0.0, 51.0)), Options(Round_trip(10000, 1)))
                )
                if(currentRoute is MutableLiveData<List<GeoPoint>>)
                {
                    val l : MutableList<GeoPoint> = ArrayList();
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

}