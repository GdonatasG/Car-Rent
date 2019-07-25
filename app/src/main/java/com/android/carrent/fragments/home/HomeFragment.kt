package com.android.carrent.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.Car.Car
import com.android.carrent.models.User
import com.android.carrent.utils.*
import com.android.carrent.utils.Constants.FASTEST_INTERVAL
import com.android.carrent.utils.Constants.FIRESTORE_USERS_REFERENCE
import com.android.carrent.utils.Constants.LAT_LNG_BOUNDS_OF_LITHUANIA
import com.android.carrent.utils.Constants.LOCATION_PERMISSIONS_REQUEST
import com.android.carrent.utils.Constants.MAPVIEW_BUNDLE_KEY
import com.android.carrent.utils.Constants.UPDATE_INTERVAL
import com.android.carrent.utils.Constants.PERMISSIONS
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.view.*

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private var TAG: String = "HomeActivity"
    private var typeOfSort: Int = 0
    // Is first attempt of location request
    private var isFirstAttempt = true
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
    // GoogleMap
    private lateinit var mGoogleMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init firebase
        mAuth = FirebaseAuth.getInstance()

        if (mAuth?.currentUser == null) {
            Log.d(TAG, "User is not logged in, starting MainActivity")
            startMainActivity()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_home, container, false)

        // toolbar
        setHasOptionsMenu(true)

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
        val ref = FirebaseFirestore.getInstance().collection(FIRESTORE_USERS_REFERENCE).document(mAuth?.uid.toString())

        ref
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    (activity as AppCompatActivity).supportActionBar?.title = user?.balance.toString() + " " +
                            resources.getText(R.string.nav_header_currency_euro)
                } else {
                    Log.d(TAG, "Snapshot data: null")
                }


            }
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
        // Move camera to device location if it`s first attempt of location request
        if (isFirstAttempt) {
            setCameraView(googleMap = mGoogleMap, location = p0)
            isFirstAttempt = false
        }
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
        var menuInflater: MenuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.home_main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_search -> {
            }
            R.id.btn_sort -> {
                view?.let {
                    selectSortView(it)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        var menuInflater: MenuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.home_sort_menu, menu)
        var action_all: MenuItem = menu.findItem(R.id.action_all)
        var action_nearest: MenuItem = menu.findItem(R.id.action_nearest)

        if (typeOfSort == 1) {
            action_all.setChecked(true)
        } else if (typeOfSort == 2) {
            action_nearest.setChecked(true)
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_all -> {
                typeOfSort = 1
                println("clicked on action all")
            }
            R.id.action_nearest -> {
                typeOfSort = 2
                println("clicked on action nearest")
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun selectSortView(v: View) {
        registerForContextMenu(v)
        activity?.openContextMenu(v)
    }

    private fun startMainActivity() {
        activity?.finish()
        startActivity(Intent(activity, MainActivity::class.java))
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
