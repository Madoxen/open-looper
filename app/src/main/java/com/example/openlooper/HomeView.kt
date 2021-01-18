package com.example.openlooper

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.openlooper.VM.FavoriteVM
import com.example.openlooper.VM.RouteVM
import com.example.openlooper.VM.adapter.AdapterFavRoute
import com.example.openlooper.model.Favorite
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_home_view.view.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class HomeView : Fragment() {

    val vm: RouteVM by viewModels()
    val vmFav: FavoriteVM by viewModels()
    private lateinit var mService: LocationRecorderService
    private var mBound: Boolean = false

    lateinit var map: MapView
    var locationOverlay: MyLocationNewOverlay? = null
    lateinit var mBottomAppBar: BottomAppBar
    lateinit var mBottomSheet: LinearLayout
    lateinit var mBottomBehavior: BottomSheetBehavior<View>
    lateinit var mBottomFAB: FloatingActionButton
    lateinit var mBottomNavigationView: BottomNavigationView
    lateinit var mAddFavouriteFAB: FloatingActionButton
    lateinit var mFavoriteSide: NavigationView
    lateinit var mDistanceText: TextView
    lateinit var track: Polyline
    var isRecording = false


    /*SERVICES*/

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationRecorderService.LocationRecorderBinder
            mService = binder.getService()
            mBound = true
            //Get back our recording data
            if (mService.isRecording) //if we even care about the route
                vm.setRoute(mService.getRecordedRoute())


            //Check if we already have a permission to access fine location
            if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                onLocationRequestAllowed() //if we do, set location overlay
            } else {
                //if we dont, ask user for permission
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_CODE
                )
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            mService.removeLocationChangedListener(::onLocationChanged)
        }
    }

    override fun onStart() {
        super.onStart()
        //This binds our client to the service
        Intent(requireContext(), LocationRecorderService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

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
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view)
        mFavoriteSide = view.findViewById((R.id.favorite_side))
        mDistanceText = view.findViewById(R.id.distance_textView)
        mAddFavouriteFAB = view.findViewById(R.id.add_to_favorite_fab)
        //Remove weird navbar view
        mBottomAppBar.bottom_nav_view.background = null
        track = Polyline()


        //Set some defaults
        //Set preferences (e.g user-agent for osmdroid)
        Configuration.getInstance()
            .load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context))

        map = view.findViewById(R.id.mapview)
        map.controller.setZoom(16.0)
        map.controller.setCenter(GeoPoint(51.0, 0.0))
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.setMultiTouchControls(true)
        map.overlayManager.add(track)

        //Observe changes to the track
        vm.currentRoute.observe(viewLifecycleOwner, Observer { t ->
            track.setPoints(t)
            val distance = vm.getRouteTotalLength()
            mDistanceText.text = "%.2f".format(distance) + "km"
            Log.e("Route changed", t.count().toString())
        })

        mBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomFAB.setOnClickListener {
            FAB_FindRoute()
        }

        mAddFavouriteFAB.setOnClickListener() {
            vm.currentRoute.value?.let {
                vmFav.addFavorite(Favorite(0,"New favourite route!",vm.getRouteTotalLength(), it))
            }
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.favorite_buttom -> {
                    mBottomFAB.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_find_replace_24
                        )
                    )
                    mBottomFAB.setOnClickListener {
                        FAB_FindRoute()
                    }
                }
                R.id.record_buttom -> {

                    if (!mService.isRecording) {
                        mBottomFAB.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_not_started_24
                            )
                        )
                    } else {
                        mBottomFAB.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_stop_circle_24
                            )
                        )
                    }

                    mBottomFAB.setOnClickListener {
                        FAB_ToggleRecord()
                    }
                }
            }
            true
        }

        // Recyclerview
        val adapter = AdapterFavRoute()
        val recyclerView = view.favorite_list_view
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        vmFav.readAllFavorite.observe(viewLifecycleOwner, Observer { fav ->
            adapter.setData(fav)
        })


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

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(connection)
        mBound = false
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
            val provider = GpsMyLocationProvider(requireContext())
            val overlay = MyLocationNewOverlay(provider, map)
            overlay.enableMyLocation()
            overlay.enableFollowLocation()
            map.overlays.add(overlay)
            map.controller.setCenter(overlay.myLocation)
            locationOverlay = overlay
            mService.addLocationChangedListener(::onLocationChanged)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            onLocationRequestAllowed()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun FAB_FindRoute() {
        mService.stopRecording()
        mService.resetRecording()
        vm.clearRoute()
        vm.lastPoint = locationOverlay?.myLocation
        vm.lastPoint?.let { vm.getNewRoute(it) }
    }

    private fun FAB_ToggleRecord() {
        val i = Intent(requireActivity(), LocationRecorderService::class.java)
        requireActivity().startService(i)

        if (mService.isRecording) {
            mService.stopRecording()
            mService.resetRecording()
            vm.clearRoute()
            mBottomFAB.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_not_started_24
                )
            )
        } else {
            mService.startRecording()
            mBottomFAB.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_stop_circle_24
                )
            )
        }


    }

    fun onLocationChanged(location: Location) {
        val point = GeoPoint(location.latitude, location.longitude)
        if (mService.isRecording)
            vm.addPoint(point)
        vm.lastPoint = point
    }

}