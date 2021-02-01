package com.example.openlooper

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import kotlinx.android.synthetic.main.favorite_add_dialog.view.*
import kotlinx.android.synthetic.main.fragment_home_view.view.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.math.BigDecimal
import java.math.RoundingMode


class HomeView : Fragment() {

    val vm: RouteVM by viewModels()
    val vmFav: FavoriteVM by viewModels()
    private lateinit var mService: LocationRecorderService
    private var mBound: Boolean = false

    lateinit var map: MapView
    var locationOverlay: MyLocationNewOverlay? = null
    lateinit var mBottomAppBar: BottomAppBar
    lateinit var mBottomFAB: FloatingActionButton
    lateinit var mBottomNavigationView: BottomNavigationView
    lateinit var mAddFavouriteFAB: FloatingActionButton
    lateinit var mFavoriteSide: NavigationView
    lateinit var mDistanceText: TextView
    lateinit var track: Polyline
    lateinit var mDistanceBar: SeekBar;
    lateinit var mRouteLengthTextView: TextView;
    lateinit var mBottomSheetBehaviour: BottomSheetBehavior<ConstraintLayout>;
    lateinit var mFollowButton: Button
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
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view)
        mFavoriteSide = view.findViewById((R.id.favorite_side))
        mDistanceText = view.findViewById(R.id.distance_textView)
        mAddFavouriteFAB = view.findViewById(R.id.add_to_favorite_fab)
        mDistanceBar = view.findViewById(R.id.distance_seekBar)
        mRouteLengthTextView = view.findViewById(R.id.routeLength_textView)
        mBottomSheetBehaviour =
            BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet_options_modal));
        //Remove weird navbar view
        mBottomAppBar.bottom_nav_view.background = null
        mFollowButton = view.findViewById(R.id.follow_button)
        track = Polyline()


        mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN;
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
        })

        mBottomFAB.setOnClickListener {
            FAB_FindRoute()
        }

        mAddFavouriteFAB.setOnClickListener() {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = inflater.inflate(R.layout.favorite_add_dialog, null)
            inflater.fav_disc.setText(
                "%.2f".format(vm.getRouteTotalLength()) + "km"
            )

            builder.setView(inflater)
            builder.setPositiveButton("Add Road") { _, _ ->

                vm.currentRoute.value?.let {
                    if (vm.getRouteTotalLength() != 0.0) {
                        vmFav.addFavorite(
                            Favorite(
                                0,
                                "${inflater.fav_name_edit.text}",
                                vm.getRouteTotalLength(),
                                it,
                                0.0 // HERE PASTE MAX HIGHT !!!
                            )
                        )
                    } else {
                        Toast.makeText(this.context, "This route is empty !", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            builder.setNegativeButton("Cancel") { _, _ -> }
            builder.create().show()
        }

        mBottomFAB.setOnLongClickListener()
        {
            mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED;
            true
        }

        mBottomSheetBehaviour.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {

                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            val p: CoordinatorLayout.LayoutParams =
                                mBottomFAB.layoutParams as CoordinatorLayout.LayoutParams;
                            p.anchorId = R.id.bottom_sheet_options_modal;
                            mBottomFAB.layoutParams = p;
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            val p: CoordinatorLayout.LayoutParams =
                                mBottomFAB.layoutParams as CoordinatorLayout.LayoutParams;
                            p.anchorId = R.id.bottom_sheet_options_modal;
                            mBottomFAB.layoutParams = p;
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                            val p: CoordinatorLayout.LayoutParams =
                                mBottomFAB.layoutParams as CoordinatorLayout.LayoutParams;
                            p.anchorId = R.id.bottom_sheet_options_modal;
                            mBottomFAB.layoutParams = p;
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            val p: CoordinatorLayout.LayoutParams =
                                mBottomFAB.layoutParams as CoordinatorLayout.LayoutParams;
                            p.anchorId = R.id.bottom_app_bar;
                            mBottomFAB.layoutParams = p;
                        }
                    }

                }
            }
        )


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


        mBottomAppBar.setOnClickListener() {
            Log.d("OpenLooper", "Clicked bottom app bar");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDistanceBar.min = 100
        };

        mDistanceBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                mRouteLengthTextView.text = (progress / 1000.0).toString() + " km";
                vm.distance.value = progress;
            }
        })

        // Recyclerview
        val adapter = AdapterFavRoute({
            vm.setRoute(it.coordinates)
        }, {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = inflater.inflate(R.layout.favorite_add_dialog, null)
            inflater.fav_disc.setText(
                "${BigDecimal(it.distance).setScale(2, RoundingMode.HALF_EVEN)} km"
            )
            inflater.fav_name_edit.setText(it.name);

            builder.setView(inflater)
            builder.setPositiveButton("Save") { _, _ ->
                val fav = it;
                vm.currentRoute.value?.let {
                    vmFav.updateFavorite(
                        Favorite(
                            fav.id,
                            "${inflater.fav_name_edit.text}",
                            fav.distance,
                            fav.coordinates,
                            0.0 // HERE PASTE MAX HIGHT !!!
                        )
                    )
                }
            }
            builder.setNegativeButton("Delete") { _, _ ->
                val fav = it;
                vm.currentRoute.value?.let {
                    vmFav.deleteFavorite(fav)
                }
            }
            builder.create().show()
        })
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

            //Add on click follow botton
            mFollowButton.setOnClickListener {
                map.controller.setCenter(overlay.myLocation)
                overlay.enableFollowLocation()
            }
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
            mBottomFAB.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_not_started_24
                )
            )
        } else {
            mService.resetRecording()
            vm.clearRoute()
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
