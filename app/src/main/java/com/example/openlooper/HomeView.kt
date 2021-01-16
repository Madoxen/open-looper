package com.example.openlooper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.openlooper.VM.RouteVM
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_home_view.*
import kotlinx.android.synthetic.main.fragment_home_view.view.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

const val REQUEST_LOCATION_CODE = 1000;

class HomeView : Fragment(), LocationListener {

    val vm: RouteVM by viewModels();
    lateinit var locationManager : LocationManager;

    lateinit var map: MapView;
    var locationOverlay: MyLocationNewOverlay? = null;
    lateinit var mBottomAppBar: BottomAppBar;
    lateinit var mBottomSheet: LinearLayout;
    lateinit var mBottomBehavior: BottomSheetBehavior<View>;
    lateinit var mBottomFAB: FloatingActionButton
    lateinit var mBottomNavigationView: BottomNavigationView
    lateinit var mFavoriteSide: NavigationView
    var isRecording = false;

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

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager;


        //Swipe bottom menu
        mBottomAppBar = view.findViewById(R.id.bottom_app_bar)
        mBottomFAB = view.findViewById(R.id.bottom_FAB)
        mBottomSheet = view.findViewById(R.id.bottom_sheet_swipe)
        mBottomBehavior = BottomSheetBehavior.from(mBottomSheet.findViewById(R.id.bottom_sheet_swipe))
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view)
        mFavoriteSide = view.findViewById((R.id.favorite_side))
        //Remove weird navbar view
        mBottomAppBar.bottom_nav_view.setBackground(null)


        //Set some defaults
        //Set preferences (e.g user-agent for osmdroid)
        Configuration.getInstance()
            .load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context));

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


        mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)



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

        mBottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.favorite_buttom -> {
                    mBottomFAB.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_find_replace_24))
                    mBottomFAB.setOnClickListener {
                        FAB_FindRoute();
                    }
                }
                R.id.record_buttom ->  {
                    mBottomFAB.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_not_started_24))
                    mBottomFAB.setOnClickListener {
                        FAB_ToggleRecord();
                    }
                }
            }
            true
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

    @SuppressLint("MissingPermission")
    private fun onLocationRequestAllowed() {
        if (!map.overlays.contains(locationOverlay)) {
            val provider = GpsMyLocationProvider(requireContext());
            val overlay = MyLocationNewOverlay(provider, map);
            overlay.enableMyLocation();
            overlay.enableFollowLocation();
            map.overlays.add(overlay);
            map.controller.setCenter(overlay.myLocation);
            locationOverlay = overlay;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5.0f, this)
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

    override fun onLocationChanged(location: Location) {
        Toast.makeText(requireContext(), location.longitude.toString() + "  " +  location.latitude.toString(), Toast.LENGTH_LONG).show();
        vm.lastPoint = GeoPoint(location.latitude, location.longitude)
        vm.addPoint(GeoPoint(location.latitude, location.longitude))
    }


    private fun FAB_FindRoute()
    {
        vm.lastPoint?.let { vm.getNewRoute(it) };
    }

    private fun FAB_ToggleRecord()
    {

    }
}