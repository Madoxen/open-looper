package com.example.openlooper

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
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


class HomeView : Fragment() {

    val vm : RouteVM by viewModels();
    lateinit var map : MapView;
    lateinit var mBottomAppBar: BottomAppBar;
    lateinit var mBottomSheet: LinearLayout;
    lateinit var mBottomBehavior: BottomSheetBehavior<View>;
    lateinit var mBottomFAB: FloatingActionButton
    lateinit var mBottomNavigationView: BottomNavigationView
    lateinit var mFavoriteSide: NavigationView

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
        mBottomBehavior = BottomSheetBehavior.from(mBottomSheet.findViewById(R.id.bottom_sheet_swipe))
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view)
        mFavoriteSide = view.findViewById((R.id.favorite_side))

        //Remove weird navbar view
        mBottomAppBar.bottom_nav_view.setBackground(null)

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
            map.overlayManager.add(track);
        })

        vm.getRoute();


        mBottomBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        mBottomFAB.setOnClickListener {
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.favorite_buttom -> view.drawer_layout_home.openDrawer(GravityCompat.START)
                R.id.record_buttom -> Log.v("Route", "There record your route")
            }
            true
        }

        return view
    }


}