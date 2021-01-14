package com.example.openlooper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


class HomeView : Fragment() {

    lateinit var map : MapView;
    lateinit var mBottomAppBar: BottomAppBar;
    lateinit var mBottomSheet: LinearLayout;
    lateinit var mBottomBehavior: BottomSheetBehavior<View>;
    lateinit var mBottomFAB: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_view, container, false)

//        Configuration.getInstance().load(this.context, PreferenceManager.getDefaultSharedPreferences(this.context));
//        map = view.findViewById<MapView>(R.id.mapview);
//        map.controller.setZoom(16.0);
//        map.controller.setCenter(GeoPoint(51.0,0.0))

        //Swipe bottom menu
        mBottomAppBar = view.findViewById(R.id.bottom_app_bar)
        mBottomFAB = view.findViewById(R.id.bottom_FAB)
        mBottomSheet = view.findViewById(R.id.bottom_sheet_swipe)
        mBottomBehavior = BottomSheetBehavior.from(mBottomSheet.findViewById(R.id.bottom_sheet_swipe))

        mBottomFAB.setOnClickListener({
            mBottomBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
        })
        return view
    }


}