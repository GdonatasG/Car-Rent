package com.android.carrent.fragments.HomeFragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.adapters.CarAdapter
import com.android.carrent.firestore.car.CarListCallback
import com.android.carrent.fragments.authentication.LoginFragment
import com.android.carrent.fragments.profile.ProfileFragment
import com.android.carrent.models.Car.Car
import com.android.carrent.utils.*
import com.android.carrent.utils.constants.Constants.BACKSTACK_HOME
import com.android.carrent.utils.constants.Constants.FASTEST_INTERVAL
import com.android.carrent.utils.constants.Constants.LAT_LNG_BOUNDS_OF_LITHUANIA
import com.android.carrent.utils.constants.Constants.LOCATION_PERMISSIONS_REQUEST
import com.android.carrent.utils.constants.Constants.MAPVIEW_BUNDLE_KEY
import com.android.carrent.utils.constants.Constants.UPDATE_INTERVAL
import com.android.carrent.utils.constants.Constants.PERMISSIONS
import com.android.carrent.utils.constants.FilterConstants.filteringArray
import com.android.carrent.utils.constants.FilterConstants.filteringArrayChecked
import com.android.carrent.utils.extensions.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private var TAG: String = "MainActivity"
    // Is first attempt of location request
    private var isFirstAttempt = true
    // Filtering options
    private var filterText: String = ""
    private var isListFiltered = false
    var filteringArrayCheckedModified = filteringArrayChecked.copyOf()
    // ViewModel
    private lateinit var viewModel: HomeFragmentViewModel
    // Location
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    // Firebase
    private var mAuth: FirebaseAuth? = null
    // Widgets
    private lateinit var mMap: MapView
    // Car
    private lateinit var carAdapter: CarAdapter
    private var carList = mutableListOf<Car>()
    private var modifiedCarList = mutableListOf<Car>()
    // GoogleMap
    private lateinit var mGoogleMap: GoogleMap
    // Weight animations
    private lateinit var mLayoutWeightAnimations: LayoutWeightAnimations


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init firebase
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_home, container, false)

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()

        // toolbar
        setHasOptionsMenu(true)

        // Init ViewModel
        viewModel = ViewModelProviders.of(activity!!).get(HomeFragmentViewModel::class.java)


        // Layout for RecyclerView of Cars
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        v.rv_list.layoutManager = layoutManager

        val divider = DividerItemDecoration(v.rv_list.context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(resources.getDrawable(R.drawable.car_item_divider))
        v.rv_list.addItemDecoration(divider)

        // Layout weight animations/manipulations
        mLayoutWeightAnimations =
            LayoutWeightAnimations(mapView = v.map_container, recyclerView = v.rv_list_container, context = context!!)
        v.btn_full_screen_map.setOnClickListener(this)


        // map init, location updates
        mMap = v.mapview
        initMap(savedInstanceState)
        configureGoogleApiClient()
        mLocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mMapServiceGpsRequests = MapServiceGpsRequests(activity!!)

        mMapServiceGpsRequests.checkLocation()

        return v
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMap.onSaveInstanceState(mapViewBundle)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mMap.onCreate(mapViewBundle)

        mMap.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mGoogleMap = map
        setCameraViewWBounds(mGoogleMap, LAT_LNG_BOUNDS_OF_LITHUANIA)

        map.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                // DetailFragment soon
                return true
            }

        })

        if (checkPermissions()) {
            enableDeviceLocationWButton(map)

        } else requestLocationPermissions(LOCATION_PERMISSIONS_REQUEST)
    }

    private fun configureGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (checkPermissions()) {
            enableDeviceLocationWButton(mGoogleMap)
            startLocationUpdates()

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

            if (mLocation == null) {
                startLocationUpdates()
            }
            if (mLocation != null) {
            } else {
            }
        } else requestLocationPermissions(LOCATION_PERMISSIONS_REQUEST)

    }

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (checkPermissions()) {
            enableDeviceLocationWButton(mGoogleMap)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest, this
            )
        } else requestLocationPermissions(LOCATION_PERMISSIONS_REQUEST)

    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + p0.getErrorCode())
    }

    override fun onLocationChanged(p0: Location?) {
        viewModel.getAllCars(object : CarListCallback {
            override fun onCarListCallback(list: MutableList<Car>) {
                carList = list
                viewModel.handleListUpdates(
                    carList = carList,
                    modifiedCarList = modifiedCarList
                )

                // Checking which list is used:
                // * If modified list is used, then updating this list, setting adapter and map markers
                // * If modified list is not used, then updating default carList, setting adapter and map markers
                if (modifiedCarList.isNotEmpty() || isListFiltered) { // modified list used
                    modifiedCarList = viewModel.filterList(
                        modifiedCarList = modifiedCarList,
                        carList = carList,
                        filteringArrayCheckedModified = filteringArrayCheckedModified,
                        filterText = filterText,
                        context = context!!
                    )
                    viewModel.sortCarList(modifiedCarList, p0)
                    carAdapter = CarAdapter(modifiedCarList, context!!, p0)
                    view?.rv_list?.adapter = carAdapter
                    viewModel.addMapMarkers(
                        map = mGoogleMap,
                        list = modifiedCarList,
                        context = activity?.applicationContext
                    )
                } else { // modified list is not used
                    viewModel.sortCarList(carList, p0)
                    carAdapter = CarAdapter(carList, context!!, p0)
                    view?.rv_list?.adapter = carAdapter
                    viewModel.addMapMarkers(
                        map = mGoogleMap,
                        list = carList,
                        context = activity?.applicationContext
                    )
                }

                // If IT`S first time attempting location:
                // * Move camera to device location
                // * Let code to know that second time will not be first time
                if (isFirstAttempt) {
                    setCameraView(googleMap = mGoogleMap, location = p0)
                    isFirstAttempt = false
                }

                // Make RecyclerView visible and progressBar - not visible
                viewModel.rvListHandlerOnLocationSuccess(view?.rv_list, view?.pb_rv_list)

            }

        })


    }

    private fun requestLocationPermissions(requestCode: Int) {
        requestPermissions(PERMISSIONS.toTypedArray(), requestCode)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableDeviceLocationWButton(mGoogleMap)
                    startLocationUpdates()
                } else makeToast(resources.getString(R.string.permissions_error))
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuInflater: MenuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.home_main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_filtering -> {
                performFiltering()
            }

            R.id.btn_profile -> {
                if (mAuth?.currentUser == null) changeFragment(R.id.container_host, LoginFragment())
                else changeFragmentWithBackStack(
                    view = R.id.container_host,
                    fragment = ProfileFragment(),
                    tag = BACKSTACK_HOME
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performFiltering() {
        lateinit var dialog: AlertDialog

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL


        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
        builder.setTitle(getText(R.string.filter_dialog_title))

        val searchView = SearchView(ContextThemeWrapper(context, R.style.CustomEditTextForDialog))

        searchView.onActionViewExpanded()
        searchView.clearFocus()
        searchView.setIconifiedByDefault(false)
        val icon: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        icon.layoutParams = LinearLayout.LayoutParams(0, 0) as ViewGroup.LayoutParams?

        val editText = searchView.findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
        editText.hint = getText(R.string.searchview_hint)
        editText.setText(filterText)
        layout.addView(searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterText = newText!!
                return true
            }

        })

        builder.setView(layout, 40, 0, 40, 0)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }

        builder.setMultiChoiceItems(filteringArray, filteringArrayCheckedModified) { _, which, isChecked ->
            filteringArrayCheckedModified[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->
            filterText = editText.text.toString()
            isListFiltered = true
            carAdapter.updateAdapter(
                viewModel.filterList(
                    modifiedCarList = modifiedCarList,
                    carList = carList,
                    filteringArrayCheckedModified = filteringArrayCheckedModified,
                    filterText = filterText,
                    context = context!!
                )
            )
            viewModel.addMapMarkers(map = mGoogleMap, list = modifiedCarList, context = activity?.applicationContext)

        }

        builder.setNegativeButton("Clear") { _, _ ->
            filteringArrayCheckedModified = filteringArrayChecked.copyOf()
            modifiedCarList.clear()
            isListFiltered = false
            viewModel.rvListHandlerOnListLoading(view?.rv_list, view?.pb_rv_list)
            filterText = ""
        }

        builder.setNeutralButton("Close") { _, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()

        dialog.show()

    }

    override fun onClick(v: View?) {
        when (v) {
            btn_full_screen_map -> {
                if (mLayoutWeightAnimations.mMapLayoutState == mLayoutWeightAnimations.MAP_LAYOUT_STATE_CONTRACTED) {
                    mLayoutWeightAnimations.mMapLayoutState = mLayoutWeightAnimations.MAP_LAYOUT_STATE_EXPANDED
                    mLayoutWeightAnimations.expandMapAnimation()
                } else if (mLayoutWeightAnimations.mMapLayoutState == mLayoutWeightAnimations.MAP_LAYOUT_STATE_EXPANDED) {
                    mLayoutWeightAnimations.mMapLayoutState = mLayoutWeightAnimations.MAP_LAYOUT_STATE_CONTRACTED
                    mLayoutWeightAnimations.contractMapAnimation()
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mMap.onResume()
        // to handle camera view when backstacking
        isFirstAttempt = true
    }

    override fun onStart() {
        super.onStart()
        mMap.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        mMap.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onPause() {
        mMap.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMap.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMap.onLowMemory()
    }
}
