package com.example.openlooper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.openlooper.VM.RouteVM
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

const val REQUEST_LOCATION_CODE = 1000;

class HomeView : Fragment() {

    val vm: RouteVM by viewModels();
    lateinit var map: MapView;
    var locationOverlay: MyLocationNewOverlay? = null;
    lateinit var mBottomAppBar: BottomAppBar;
    lateinit var mBottomSheet: LinearLayout;
    lateinit var mBottomBehavior: BottomSheetBehavior<View>;
    lateinit var mBottomFAB: FloatingActionButton
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

        //Swipe bottom menu
        mBottomAppBar = view.findViewById(R.id.bottom_app_bar)
        mBottomFAB = view.findViewById(R.id.bottom_FAB)
        mBottomSheet = view.findViewById(R.id.bottom_sheet_swipe)
        mBottomBehavior =
            BottomSheetBehavior.from(mBottomSheet.findViewById(R.id.bottom_sheet_swipe))


        //Set preferences (e.g user-agent for osmdroid)
        Configuration.getInstance()
            .load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context));
        //Set some defaults
        map = view.findViewById(R.id.mapview);
        map.controller.setZoom(16.0);
        map.controller.setCenter(GeoPoint(51.0, 0.0))
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);

        //Observe changes to the track
        vm.currentRoute.observe(viewLifecycleOwner, Observer { t ->
            if (track != null)
                map.overlayManager.remove(track);

            track = Polyline();
            track?.setPoints(t)
            map.overlayManager.add(track);
        })

        //Listen to FAB clicks
        mBottomFAB.setOnClickListener {
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }

        //Check if we already have a permission to access fine location
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            onLocationRequestAllowed() //if we do, set location overlay
        } else {
            //if we dont, ask user for permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_CODE
            );
        }

        return view
    }

    override fun onPause() {
        map.onPause() //needed for compass, my location overlays, v6.0.0 and up
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun onLocationRequestAllowed() {
        if (!map.overlays.contains(locationOverlay)) {
            val overlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map);
            overlay.enableMyLocation();
            overlay.enableFollowLocation();
            map.overlays.add(overlay);
            map.controller.setCenter(overlay.myLocation);
            locationOverlay = overlay;
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            onLocationRequestAllowed();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}