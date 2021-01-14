package com.example.openlooper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


class HomeView : Fragment() {

    lateinit var map : MapView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_view, container, false)

        Configuration.getInstance().load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context));
        map = view.findViewById<MapView>(R.id.mapview);
        map.controller.setZoom(16.0);
        map.controller.setCenter(GeoPoint(51.0,0.0))

        return view
    }


}