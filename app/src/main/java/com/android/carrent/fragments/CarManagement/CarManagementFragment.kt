package com.android.carrent.fragments.CarManagement


import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.fragments.CarFragment.CarFragment
import com.android.carrent.models.Car.Car
import com.android.carrent.models.User.User
import com.android.carrent.utils.MapServiceGpsRequests
import com.android.carrent.utils.constants.Constants
import com.android.carrent.utils.constants.Constants.DATE_FORMAT
import com.android.carrent.utils.extensions.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.car_rent_layout.view.*
import kotlinx.android.synthetic.main.fragment_car_management.*
import kotlinx.android.synthetic.main.fragment_car_management.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class CarManagementFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private var TAG: String = "CarManagementFragment"
    private var isFirstTimeLoading = true
    // Context (to ensure that not null)
    private lateinit var mContext: Context

    // Location
    private lateinit var mMapServiceGpsRequests: MapServiceGpsRequests
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var flag = true

    // ViewModel
    private lateinit var viewModel: CarManagementViewModel
    private var user: MutableLiveData<User> = MutableLiveData()
    private var car: MutableLiveData<Car> = MutableLiveData()

    // Widgets
    private lateinit var mMap: MapView
    private var mToolbarTitle: String? = ""

    // GoogleMap
    private lateinit var mGoogleMap: GoogleMap

    // To confirm rent, user have to press confirm button twice in some sort of time
    private var backPressedTime: Long = 0

    private var isDateSelected = false
    private var isTimeSelected = false
    private var totalRentSum: Float = 0F

    // Car Rent Dialog
    private lateinit var dialog: AlertDialog
    private lateinit var dpd: DatePickerDialog
    private lateinit var tpd: TimePickerDialog

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog


    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()
        setHasOptionsMenu(true)
        shouldShowHomeButton(activity, true)

        configureGoogleApiClient()
        mLocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mMapServiceGpsRequests = MapServiceGpsRequests(activity!!)

        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            mAuth = it
            if (mAuth.currentUser == null && flag) {
                clearFragmentContentWhenRemoving()
                noUserPopFragment(resources.getString(R.string.message_logged_out))
                flag = false
            }
        }

        // Init progress dialog
        progressDialog = ProgressDialog(mContext, R.style.progress_dialog_bar)
        modifyProgressDialog(progressDialog)


        dpd = DatePickerDialog(mContext)

        viewModel = ViewModelProviders.of(this).get(CarManagementViewModel::class.java)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_car_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFirstTimeLoading = true


        view.btn_lock_switch.setOnClickListener(this)
        view.btn_more.setOnClickListener(this)
        view.btn_extend.setOnClickListener(this)

        mMapServiceGpsRequests.checkLocation()

        // map init, location updates
        mMap = view.mapview
        initMap(savedInstanceState)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(Constants.MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMap.onSaveInstanceState(mapViewBundle)
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }

        mMap.onCreate(mapViewBundle)

        mMap.getMapAsync(this)
    }

    private fun loadUser(uid: String?) {
        viewModel.getUser(uid!!).observe(viewLifecycleOwner, Observer<User> {
            user.value = it
            if (user.value == null) {
                clearFragmentContentWhenRemoving()
                noUserPopFragment(resources.getString(R.string.message_logged_out))
            } else {
                if (!it.rent?.hasCar!! || it.rent?.rentedCarId == 0) {
                    clearFragmentContentWhenRemoving()
                    noCarPopFragment(
                        resources.getString(
                            R.string.rent_expired
                        )
                    )
                } else loadCar(it.rent?.rentedCarId!!)
            }
        })
    }

    private fun loadCar(id: Int) {
        viewModel.getCarById(id).observe(viewLifecycleOwner, Observer<Car> {
            car.value = it
            if (car.value == null) {
                clearFragmentContentWhenRemoving()
                noCarPopFragment(resources.getString(R.string.rent_expired))
            } else {
                mToolbarTitle = it.model.title
                setToolbarTitle(mToolbarTitle)
                setUpRentInfo(it.rent.rentedUntil)
                setUpLock(it.model.locked, it.model.title)
                setUpLocation(it)
                setCalendarDateToRentedUntil(it.rent.rentedUntil)
            }
        })
    }

    private fun setUpRentInfo(rentedUntil: Timestamp?) {
        tv_rent_date.text = DATE_FORMAT.format(rentedUntil?.toDate())
    }

    private fun setUpLock(isLocked: Boolean?, title: String?) {
        isLocked?.let {
            if (it) {
                view?.iv_lock_options?.background =
                    resources.getDrawable(R.drawable.ic_lock_lightgray_24dp)
                view?.tv_lock_message?.text = title + " " + resources.getString(R.string.is_locked)
                view?.btn_lock_switch?.text = resources.getString(R.string.unlock)
            } else {
                view?.iv_lock_options?.background =
                    resources.getDrawable(R.drawable.ic_lock_open_lightgray_24dp)
                view?.tv_lock_message?.text =
                    title + " " + resources.getString(R.string.is_unlocked)
                view?.btn_lock_switch?.text = resources.getString(R.string.lock)
            }
        }
    }

    private fun setUpLocation(car: Car?) {
        val location = Location(LocationManager.NETWORK_PROVIDER)

        location.latitude = car?.location!!.latitude
        location.longitude = car.location!!.longitude
        if (isFirstTimeLoading) {
            setCameraView(googleMap = mGoogleMap, location = location)
            isFirstTimeLoading = false
        }

        viewModel.addMapMarker(map = mGoogleMap, car = car, context = mContext)

        view?.tv_car_location?.text = getAddress(
            car.location!!.latitude,
            car.location!!.longitude,
            mContext
        ).thoroughfare + " " + getAddress(
            car.location!!.latitude,
            car.location!!.longitude,
            mContext
        ).subThoroughfare + ", " + getAddress(
            car.location!!.latitude,
            car.location!!.longitude,
            mContext
        ).locality
    }

    private fun setCalendarDateToRentedUntil(rentedUntil: Timestamp?) {
        if (!isDateSelected && !isTimeSelected) selectedDate.time = rentedUntil?.toDate()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_lock_switch -> {
                if ((activity as MainActivity).isInternetOn) {
                    if (car.value?.model?.locked!!) {
                        car.value?.model?.locked = false
                        viewModel.getCarRef(car.value?.id!!).set(car.value!!)
                    } else {
                        car.value?.model?.locked = true
                        viewModel.getCarRef(car.value?.id!!).set(car.value!!)
                    }
                } else (activity as MainActivity).showNetworkMessage()
            }

            R.id.btn_more -> {
                goToCarFragment(car.value?.id)
            }

            R.id.btn_extend -> {
                if ((activity as MainActivity).isInternetOn) performExtending()
                else (activity as MainActivity).showNetworkMessage()
            }
        }
    }

    private fun goToCarFragment(id: Int?) {
        id?.let {
            val cFrag = CarFragment()
            val bundle = Bundle()
            bundle.putInt(Constants.BUNDLE_KEY_CAR_ID, it)
            cFrag.arguments = bundle

            changeFragmentWithBackstack(
                view = R.id.container_host,
                fragment = cFrag,
                tag = Constants.BACKSTACK_CAR_FRAGMENT
            )
        }
    }

    private fun performExtending() {

        val layout = LinearLayout(mContext)
        val v = layoutInflater.inflate(R.layout.car_rent_layout, null)
        v.tv_rent_title.text = resources.getString(R.string.car_extend_date)

        layout.orientation = LinearLayout.VERTICAL

        val builder = AlertDialog.Builder(ContextThemeWrapper(mContext, R.style.AlertDialogCustom))
        builder.setTitle(getText(R.string.car_extend_period))
        builder.setView(v)

        setFinalRentDateAndCost(
            v.tv_selected_date,
            v.tv_total_cost,
            totalExtendSum(selectedDate, car),
            isDateSelected,
            isTimeSelected,
            selectedDate
        )

        builder.setPositiveButton("Confirm") { _, _ -> }

        builder.setNegativeButton("Select period") { _, _ -> }

        builder.setNeutralButton("Cancel") { _, _ -> }

        dialog = builder.create()
        dialog.setCancelable(false)

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            mAuth.currentUser?.reload()
            if (backPressedTime + 3000 > System.currentTimeMillis()) {
                if ((activity as MainActivity).isInternetOn) {
                    mAuth.currentUser?.reload()
                    progressDialog.show()
                    if (mAuth.currentUser != null && user.value != null) {
                        if (car.value != null) {
                            if (areRequirementsMet()) {
                                totalRentSum = totalRentSum(selectedDate, car)
                                if (user.value?.balance!! >= totalRentSum) {
                                    val rentedUntil = Timestamp(selectedDate.time)

                                    // Updating user object values
                                    user.value?.rent?.rentedUntil = rentedUntil
                                    user.value?.balance = user.value?.balance!! - totalRentSum
                                    viewModel.getUserRef(user.value?.id!!).set(user.value!!)

                                    // Updating car object values
                                    car.value?.rent?.rentedUntil = rentedUntil
                                    viewModel.getCarRef(car.value?.id!!).set(car.value!!)

                                    isDateSelected = false
                                    isTimeSelected = false
                                    progressDialog.dismiss()
                                    dialog.dismiss()
                                    (activity as MainActivity).mSnackbar?.setText(
                                        resources.getString(
                                            R.string.time_extended
                                        )
                                    )?.show()
                                } else {
                                    progressDialog.dismiss()
                                    makeToast(resources.getString(R.string.CAR_RENT_ERROR_MONEY))
                                }
                            } else {
                                progressDialog.dismiss()
                                makeToast(resources.getString(R.string.CAR_RENT_ERROR_REQUIREMENTS_NOT_MET))
                            }
                        } else {
                            progressDialog.dismiss()
                            clearFragmentContentWhenRemoving()
                            noCarPopFragment(resources.getString(R.string.rent_expired))
                        }
                    } else {
                        progressDialog.dismiss()
                        clearFragmentContentWhenRemoving()
                        noUserPopFragment(resources.getString(R.string.message_logged_out))
                    }
                } else (activity as MainActivity).showNetworkMessage()
            } else makeToast(resources.getString(R.string.CAR_RENT_TWICE_CLICK))


            backPressedTime = System.currentTimeMillis()

        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            dpd = DatePickerDialog(
                mContext,
                DatePickerDialog.OnDateSetListener { view, i, i2, i3 ->
                    selectedDate.set(Calendar.YEAR, i)
                    selectedDate.set(Calendar.MONTH, i2)
                    selectedDate.set(Calendar.DAY_OF_MONTH, i3)
                    isDateSelected = true
                    tpd = TimePickerDialog(
                        mContext,
                        TimePickerDialog.OnTimeSetListener { timePicker, j, j2 ->
                            selectedDate.set(Calendar.HOUR_OF_DAY, j)
                            selectedDate.set(Calendar.MINUTE, j2)
                            isTimeSelected = true
                            totalRentSum = totalExtendSum(selectedDate, car)
                            setFinalRentDateAndCost(
                                v.tv_selected_date,
                                v.tv_total_cost,
                                totalRentSum,
                                isDateSelected,
                                isTimeSelected,
                                selectedDate
                            )
                        },
                        selectedDate.get(Calendar.HOUR_OF_DAY),
                        selectedDate.get(Calendar.MINUTE),
                        true
                    )
                    tpd.show()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = car.value?.rent?.rentedUntil?.seconds!! * 1000
            dpd.show()
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun clearFragmentContentWhenRemoving() {
        if (::dialog.isInitialized && dialog.isShowing) dialog.dismiss()
        if (::dpd.isInitialized && dpd.isShowing) dpd.dismiss()
        if (::tpd.isInitialized && tpd.isShowing) tpd.dismiss()
    }

    fun totalExtendSum(selectedDate: Calendar, car: MutableLiveData<Car>?): Float {
        val total: Float // Total sum
        val RENT_TIME_MINUTES =
            TimeUnit.MILLISECONDS.toMinutes(selectedDate.timeInMillis - car?.value?.rent?.rentedUntil?.seconds!! * 1000)

        // Check weekend discount
        if (isWeekend()) {
            val sum = calculateSum(car.value?.rate?.weekendRatePerH!!, RENT_TIME_MINUTES)
            if (car.value?.rate?.hasDiscountWeekend!!) total =
                sum - (sum * (calculateDiscountPercentage(car.value?.rate?.discountPercentage!!)))
            else total = sum
        }
        // Check workdays discount
        else {
            val sum = calculateSum(car.value?.rate?.workdaysRatePerH!!, RENT_TIME_MINUTES)
            if (car.value?.rate?.hasDiscountWorkdays!!) total =
                sum - (sum * (calculateDiscountPercentage(car.value?.rate?.discountPercentage!!)))
            else total = sum
        }

        return total
    }

    private fun areRequirementsMet(): Boolean {
        // Selected time should meet requirements (already rented time + MINIMUM_RENT_TIME must be less or equal to selected date and time)
        return selectedDate.timeInMillis >= car.value?.rent?.rentedUntil?.seconds!! * 1000 + Constants.MINIMUM_RENT_TIME
    }

    override fun onMapReady(map: GoogleMap?) {
        mGoogleMap = map!!

        setCameraViewWBounds(
            mGoogleMap,
            Constants.LAT_LNG_BOUNDS_OF_LITHUANIA
        )

        loadUser(mAuth.currentUser?.uid)



        if (checkPermissions()) {
            enableDeviceLocationWButton(map)

        } else requestLocationPermissions(Constants.LOCATION_PERMISSIONS_REQUEST)
    }

    private fun requestLocationPermissions(requestCode: Int) {
        requestPermissions(Constants.PERMISSIONS.toTypedArray(), requestCode)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.LOCATION_PERMISSIONS_REQUEST -> {
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

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(Constants.UPDATE_INTERVAL)
            .setFastestInterval(Constants.FASTEST_INTERVAL)
        // Request location updates
        if (checkPermissions()) {
            enableDeviceLocationWButton(mGoogleMap)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest, this
            )
        } else requestLocationPermissions(Constants.LOCATION_PERMISSIONS_REQUEST)

    }

    private fun configureGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(mContext)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }

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
        } else requestLocationPermissions(Constants.LOCATION_PERMISSIONS_REQUEST)
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + p0.errorCode)
    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener!!)

        mMap.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
        super.onStart()
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        mMap.onResume()
        shouldShowHomeButton(activity, true)
        setToolbarTitle(mToolbarTitle)
        super.onResume()
    }

    override fun onPause() {
        shouldShowHomeButton(activity, false)
        setToolbarTitle("")
        mMap.onPause()
        super.onPause()
    }

    override fun onDetach() {
        shouldShowHomeButton(activity, false)
        setToolbarTitle("")
        super.onDetach()
    }

    override fun onDestroy() {
        mMap.onDestroy()
        shouldShowHomeButton(activity, false)
        setToolbarTitle("")
        super.onDestroy()
    }

    override fun onStop() {
        if (mAuthStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(
            mAuthStateListener!!
        )

        mMap.onStop()
        if (mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMap.onLowMemory()
    }
}
