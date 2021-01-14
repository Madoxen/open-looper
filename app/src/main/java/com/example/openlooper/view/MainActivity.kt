package com.example.openlooper.view


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.openlooper.R
import com.example.openlooper.VM.RouteVM
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline


class MainActivity : AppCompatActivity() {

    val vm : RouteVM by viewModels();
    lateinit var map : MapView;
    var track: Polyline? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map = findViewById<MapView>(R.id.mapview);
        //Set some defaults
        map.controller.setZoom(16.0);
        map.controller.setCenter(GeoPoint(51.0,0.0))
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);


        vm.currentRoute.observe(this, Observer {t ->
            if(track != null)
                map.overlayManager.remove(track);

            track = Polyline();
            track?.setPoints(t)
            map.overlayManager.add(track); //TODO: remove existing track
        })


        vm.getRoute();
    }
}