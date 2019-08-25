package com.android.carrent.fragments.logged.HomeFragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.android.carrent.adapters.CarAdapter
import com.android.carrent.firestore.car.CarListCallback
import com.android.carrent.fragments.unlogged.LoginFragment
import com.android.carrent.models.Car.Car
import com.android.carrent.models.ClusterMarker
import com.android.carrent.utils.*
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_home.view.*

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

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
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var mClusterManagerRenderer: ClusterManagerRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting LoginFragment")
            changeFragment(fragment = LoginFragment())
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_home, container, false)

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


        // Init user balance into toolbar
        initBalance()

        // map init, location updates
        mMap = v.mapview
        initMap(savedInstanceState)

        configureGoogleApiClient()
        mLocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mMapServiceGpsRequests = MapServiceGpsRequests(activity!!)

        mMapServiceGpsRequests.checkLocation()

        return v
    }

    private fun initBalance() {
        viewModel.getUser(mAuth!!.uid.toString()).observe(this, Observer { it ->
            (activity as AppCompatActivity).supportActionBar?.title = it.balance.toString() + " " +
                    resources.getText(R.string.nav_header_currency_euro)
        })
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

                // Sorting used list
                if (modifiedCarList.isNotEmpty() || isListFiltered) {
                    modifiedCarList = viewModel.filterList(
                        modifiedCarList = modifiedCarList,
                        carList = carList,
                        filteringArrayCheckedModified = filteringArrayCheckedModified,
                        filterText = filterText,
                        context = context!!
                    )
                    viewModel.sortCarList(modifiedCarList, p0)
                }
                viewModel.sortCarList(carList, p0)

                // If IT`S first attempt of getting carList:
                // * Setting up adapter
                // * Attaching that adapter to RecyclerView
                // * Adding markers
                // * Move camera to device location
                // * Let code to know that second time will not be first time
                if (isFirstAttempt) {
                    // Setting adapter for car recycler view
                    carAdapter = CarAdapter(carList, context!!, p0)
                    view?.rv_list?.adapter = carAdapter
                    addMarkers(carList)

                    setCameraView(googleMap = mGoogleMap, location = p0)
                    isFirstAttempt = false
                }
                // If IT`S NOT first attempt of getting carList:
                // * Checking which list should be used for adapter
                // * Adding markers
                else {
                    if (modifiedCarList.isNotEmpty() || isListFiltered) {
                        carAdapter.updateAdapter(modifiedCarList, p0)
                        addMarkers(modifiedCarList)
                    } else {
                        carAdapter.updateAdapter(carList, p0)
                        addMarkers(carList)
                    }
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

    private fun addMarkers(list: MutableList<Car>) {
        mGoogleMap.let {
            mClusterManager = ClusterManager(activity?.applicationContext, mGoogleMap)

            mClusterManagerRenderer = ClusterManagerRenderer(activity, mGoogleMap, mClusterManager)

            mClusterManager.renderer = mClusterManagerRenderer

            mClusterManager.clearItems()
            mGoogleMap.clear()

            for (c in list) {
                val title: String? = c.model.title
                // Snippet is id of car, because I want to start DetailFragment when marker will be clicked.
                val snippet: String? = c.id.toString()

                val icon =
                    if (c.rent.rented!!) R.drawable.ic_directions_car_rented_24dp else R.drawable.ic_directions_car_free_24dp

                val marker =
                    ClusterMarker(snippet!!, title!!, LatLng(c.location!!.latitude, c.location!!.longitude), icon)
                mClusterManager.addItem(marker)
            }
            mClusterManager.cluster()

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
        icon.layoutParams = LinearLayout.LayoutParams(0, 0)

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
            addMarkers(modifiedCarList)

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

    override fun onResume() {
        super.onResume()
        mMap.onResume()
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
