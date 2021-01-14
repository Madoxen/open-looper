package com.example.openlooper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.openlooper.VM.RouteVM
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline


class HomeView : Fragment() {

    val vm : RouteVM by viewModels();
    lateinit var map : MapView;
    var track: Polyline? = null;

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
        map = view.findViewById(R.id.mapview);
        //Set some defaults
        map.controller.setZoom(16.0);
        map.controller.setCenter(GeoPoint(51.0,0.0))
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);

        vm.currentRoute.observe(viewLifecycleOwner, Observer {t ->
            if(track != null)
                map.overlayManager.remove(track);

            track = Polyline();
            track?.setPoints(t)
            map.overlayManager.add(track); //TODO: remove existing track
        })


        vm.getRoute();

        return view
    }


}