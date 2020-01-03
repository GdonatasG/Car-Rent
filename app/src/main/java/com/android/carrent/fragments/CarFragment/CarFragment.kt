package com.android.carrent.fragments.CarFragment


import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment

import com.android.carrent.R
import com.android.carrent.activities.MainActivity
import com.android.carrent.models.Car.Car
import com.android.carrent.utils.constants.Constants
import com.android.carrent.utils.constants.Constants.BUNDLE_KEY_CAR_ID
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.car_main_details_layout.view.*
import kotlinx.android.synthetic.main.car_fuel_consumption_layout.view.*
import kotlinx.android.synthetic.main.car_location_layout.view.*
import kotlinx.android.synthetic.main.fragment_car.view.*
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.android.carrent.models.User.User
import com.android.carrent.utils.constants.Constants.DATE_FORMAT
import com.android.carrent.utils.extensions.*
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.car_rent_layout.view.*
import java.util.*
import java.util.concurrent.TimeUnit


class CarFragment : Fragment(), View.OnClickListener, OnMapReadyCallback {

    // View of Fragment
    private var mView: View? = null

    // Context (to ensure that not null)
    private lateinit var mContext: Context

    // GoogleMap
    private lateinit var mGoogleMap: GoogleMap

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    // Widgets
    private lateinit var mMap: MapView

    // ViewModel
    private lateinit var viewModel: CarFragmentViewModel

    private var CAR_ID: Int? = null
    private var car: MutableLiveData<Car> = MutableLiveData()
    private var user: MutableLiveData<User> = MutableLiveData()

    // To confirm rent, user have to press confirm button twice in some sort of time
    private var backPressedTime: Long = 0

    private var isDateSelected = false
    private var isTimeSelected = false
    private var HOUR_TO_MILLIS = 3600000 // 1 hour in milliseconds
    private var totalRentSum: Float = 0F
    // Minimum time of rent in milliseconds
    private var MINIMUM_RENT_TIME =
        1 * HOUR_TO_MILLIS - TimeUnit.SECONDS.toMillis(40) // Some time given to user to make a decision

    // Car Rent Dialog
    private lateinit var dialog: AlertDialog
    private lateinit var dpd: DatePickerDialog
    private lateinit var tpd: TimePickerDialog

    // ProgressDialog
    private lateinit var progressDialog: ProgressDialog


    val selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            mAuth = it
        }


        // Enable needed widgets
        (activity as MainActivity).enabledWidgets()
        setHasOptionsMenu(true)
        shouldShowHomeButton(activity, true)

        // Init ViewModel
        viewModel = ViewModelProviders.of(this).get(CarFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_car, container, false)

        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = arguments
        CAR_ID = bundle?.getInt(BUNDLE_KEY_CAR_ID)

        // Init progress dialog
        progressDialog = ProgressDialog(mContext, R.style.progress_dialog_bar)
        modifyProgressDialog(progressDialog)

        // On-clicks
        mView?.car_main_details?.arrowBtnCarMain?.setOnClickListener(this)
        mView?.car_fuel_consumption?.arrowBtnCarFuelConsumption?.setOnClickListener(this)
        mView?.car_location?.arrowBtnCarLocation?.setOnClickListener(this)
        mView?.btn_rent?.setOnClickListener(this)

        // Loading user data
        loadUser(mAuth.currentUser?.uid)

        // Loading Location layout map
        mMap = mView?.mapview!!
        initMap(savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadUser(uid: String?) {
        uid?.let {
            viewModel.getUser(it)
                .observe(viewLifecycleOwner, Observer<User> {
                    user.value = it
                })
        }

    }

    private fun loadCar(id: Int?) {
        viewModel.getCarById(id).observe(viewLifecycleOwner, Observer<Car> {
            car.value = it
            if (car.value == null) {
                makeToast(resources.getString(R.string.ERROR_NO_CAR))
                // Removing dialog
                clearFragmentContentWhenRemoving()
                activity?.supportFragmentManager?.popBackStack()
                //removeFragment(this)
            } else {
                setToolbarTitle(it.model.title)
                loadCarImage(it.model.photoUrl)
                setUpCarMainDetails(it)
                setUpCarConsumption(it)
                setUpCarLocation(it)
                setUpRentButton(it.rent.rented)
            }
        })
    }

    private fun clearFragmentContentWhenRemoving() {
        if (::dialog.isInitialized && dialog.isShowing) dialog.dismiss()
        if (::dpd.isInitialized && dpd.isShowing) dpd.dismiss()
        if (::tpd.isInitialized && tpd.isShowing) tpd.dismiss()
    }

    private fun loadCarImage(url: String?) {
        Glide.with(activity!!)
            .load(url)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    mView?.car_picture?.background = resource
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    mView?.car_picture?.background = resources.getDrawable(R.color.lightgray)
                }
            })
    }

    private fun setUpCarMainDetails(car: Car?) {
        mView?.car_main_details?.tv_model_title?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_model_title) + " " + detailCarColorBold(
                    car?.model?.title
                )
            )

        mView?.car_main_details?.tv_platenumber?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_plate_number) + " " + detailCarColorBold(
                    car?.plateNumber
                )
            )

        mView?.car_main_details?.tv_model_engine?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_model_engine) + " " + detailCarColorBold(
                    car?.model?.engine
                )
            )

        mView?.car_main_details?.tv_gas_type?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_gas_type) + " " + detailCarColorBold(
                    car?.model?.gasType
                )
            )

        mView?.car_main_details?.tv_release_date?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_release_date) + " " + detailCarColorBold(
                    car?.model?.releaseDate
                )
            )
    }

    private fun setUpCarConsumption(car: Car?) {
        mView?.car_fuel_consumption?.tv_capacity?.text =
            Html.fromHtml(
                resources.getString(R.string.car_full_tank_capacity) + " " + detailCarColorBold(
                    car?.model?.fullTankCapacity.toString()
                ) + " " + resources.getString(R.string.gas_measure_liter)
            )

        mView?.car_fuel_consumption?.tv_tank_left?.text =
            Html.fromHtml(
                resources.getString(R.string.car_tank_left) + " " + detailCarColorBold(
                    car?.model?.tankLeft.toString()
                ) + " " + resources.getString(R.string.gas_measure_liter)
            )

        mView?.car_fuel_consumption?.tv_consumption_urban?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_consumption_urban) + " " + detailCarColorBold(
                    car?.model?.averageFuelConsCity.toString()
                ) + " " + resources.getString(R.string.gas_measure_liter)
            )

        mView?.car_fuel_consumption?.tv_consumption_combined?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_consumption_combined) + " " + detailCarColorBold(
                    car?.model?.averageFuelConsMixed.toString()
                ) + " " + resources.getString(R.string.gas_measure_liter)
            )

        mView?.car_fuel_consumption?.tv_consumption_highway?.text =
            Html.fromHtml(
                resources.getString(com.android.carrent.R.string.car_consumption_highway) + " " + detailCarColorBold(
                    car?.model?.averageFuelConsOut.toString()
                ) + " " + resources.getString(R.string.gas_measure_liter)
            )
    }

    private fun setUpCarLocation(car: Car?) {
        val location = Location(LocationManager.NETWORK_PROVIDER)

        location.latitude = car?.location!!.latitude
        location.longitude = car.location!!.longitude
        setCameraView(googleMap = mGoogleMap, location = location)

        viewModel.addMapMarker(map = mGoogleMap, car = car, context = mContext)

        mView?.car_location?.tv_located?.text =
            Html.fromHtml(
                resources.getString(R.string.car_located) + " " + detailCarColorBold(
                    getAddress(
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
                )
            )
    }

    private fun setUpRentButton(rented: Boolean?) {
        rented?.let {
            if (it) mView?.btn_rent?.background?.setTint(resources.getColor(com.android.carrent.R.color.car_rented))
            else mView?.btn_rent?.background?.setTint(resources.getColor(com.android.carrent.R.color.car_free))
        }
    }

    private fun detailCarColorBold(text: String?): String {

        return "<b><font color=" + resources.getColor(com.android.carrent.R.color.car_rented) + ">" + text + "</font></b>"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Main expandable Card View of car details
            com.android.carrent.R.id.arrowBtnCarMain -> {
                viewModel.handleExpandableView(
                    expandableView = mView?.car_main_details?.expandableViewCarMain,
                    cardView = mView?.car_main_details?.cardViewCarMain,
                    arrowButton = mView?.car_main_details?.cardViewCarMain?.arrowBtnCarMain
                )
            }

            // Expandable Card View of car fuel consumption
            com.android.carrent.R.id.arrowBtnCarFuelConsumption -> {
                viewModel.handleExpandableView(
                    expandableView = mView?.car_fuel_consumption?.expandableViewCarFuelConsumption,
                    cardView = mView?.car_fuel_consumption?.cardViewCarFuelConsumption,
                    arrowButton = mView?.car_fuel_consumption?.cardViewCarFuelConsumption?.arrowBtnCarFuelConsumption
                )
            }

            // Expandable Card View of car location
            com.android.carrent.R.id.arrowBtnCarLocation -> {
                viewModel.handleExpandableView(
                    expandableView = mView?.car_location?.expandableViewCarLocation,
                    cardView = mView?.car_location?.cardViewCarLocation,
                    arrowButton = mView?.car_location?.cardViewCarLocation?.arrowBtnCarLocation
                )
            }

            com.android.carrent.R.id.btn_rent -> {
                mAuth.currentUser?.reload()
                if (mAuth.currentUser == null) {
                    noUserGoToLogin(view = R.id.container_host, context = mContext)
                } else {
                    if (isRentedOrHaveRented()) makeToast(
                        resources.getString(
                            R.string.CAR_RENT_ERROR_RENTED_OR_HAS_CAR
                        )
                    )
                    else {
                        performRent()
                    }
                }
            }
        }
    }

    private fun performRent() {

        val layout = LinearLayout(mContext)
        val v = layoutInflater.inflate(R.layout.car_rent_layout, null)
        layout.orientation = LinearLayout.VERTICAL


        val builder = AlertDialog.Builder(ContextThemeWrapper(mContext, R.style.AlertDialogCustom))
        builder.setTitle(getText(R.string.car_rent_period))
        builder.setView(v)

        setFinalRentDateAndCost(v.tv_selected_date, v.tv_total_cost, totalRentSum())

        builder.setPositiveButton("Confirm") { _, _ -> }

        builder.setNegativeButton("Select period") { _, _ -> }

        builder.setNeutralButton("Cancel") { _, _ -> }

        dialog = builder.create()
        dialog.setCancelable(false)

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // User have to click confirm button twice in some sort of time to ensure rent
            mAuth.currentUser?.reload()
            if (backPressedTime + 3000 > System.currentTimeMillis()) {
                // To rent car, user:
                // Must be connected to the internet
                // Must be logged in
                // Must meet the rent time requirements,
                // Mustn`t have rented car or aimed car mustn`t be rented
                // Must have enough money in the balance
                if ((activity as MainActivity).isInternetOn) {
                    mAuth.currentUser?.reload()
                    progressDialog.show()
                    if (mAuth.currentUser != null && user.value != null) {
                        if (areRequirementsMet()) {
                            if (!isRentedOrHaveRented()) {
                                totalRentSum = totalRentSum()
                                if (user.value?.balance!! >= totalRentSum) {
                                    val rentStarted = Timestamp(Calendar.getInstance().time)
                                    val rentedUntil = Timestamp(selectedDate.time)

                                    // Updating user object values
                                    user.value?.rent?.hasCar = true
                                    user.value?.rent?.rentedCarId = car.value?.id
                                    user.value?.rent?.rentStarted = rentStarted
                                    user.value?.rent?.rentedUntil = rentedUntil
                                    user.value?.balance = user.value?.balance!! - totalRentSum

                                    // Updating car object values
                                    car.value?.rent?.rented = true
                                    car.value?.rent?.rentStarted = rentStarted
                                    car.value?.rent?.rentedUntil = rentedUntil
                                    car.value?.rent?.rentedById = user.value?.id

                                    // Car will be rented if User && Car already exists in Firestore Collections
                                    viewModel.getCarRef(car.value?.id).get().addOnCompleteListener {
                                        if (it.result?.exists()!!) {
                                            viewModel.getUserRef(user.value?.id!!).get()
                                                .addOnCompleteListener { u ->
                                                    if (u.result?.exists()!!) {
                                                        viewModel.getUserRef(user.value?.id!!)
                                                            .set(user.value!!)
                                                        viewModel.getCarRef(car.value?.id)
                                                            .set(car.value!!)

                                                        progressDialog.dismiss()
                                                        // Start CarManagementFragment (SOON)
                                                        makeToast("CAR RENTED / TOAST WILL BE DELETED.")

                                                    } else {
                                                        progressDialog.dismiss()
                                                        makeToast("ERROR NO USER / DELETE SOON")
                                                    }
                                                }
                                        } else {
                                            progressDialog.dismiss()
                                            makeToast("ERROR NO CAR / DELETE SOON")
                                        }
                                    }

                                } else {
                                    progressDialog.dismiss()
                                    makeToast(resources.getString(R.string.CAR_RENT_ERROR_MONEY))
                                }
                            } else {
                                progressDialog.dismiss()
                                makeToast(resources.getString(R.string.CAR_RENT_ERROR_RENTED_OR_HAS_CAR))
                            }
                        } else {
                            progressDialog.dismiss()
                            makeToast(resources.getString(R.string.CAR_RENT_ERROR_REQUIREMENTS_NOT_MET))
                        }
                    } else {
                        dialog.dismiss()
                        progressDialog.dismiss()
                        noUserGoToLogin(view = R.id.container_host, context = mContext)
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
                            totalRentSum = totalRentSum()
                            setFinalRentDateAndCost(
                                v.tv_selected_date,
                                v.tv_total_cost,
                                totalRentSum
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
            dpd.show()
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun isRentedOrHaveRented(): Boolean {
        return if (car.value != null && user.value != null) car.value?.rent?.rented!! || user.value?.rent?.hasCar!!
        else false
    }

    private fun totalRentSum(): Float {
        var total: Float // Total sum
        val RENT_TIME_MINUTES =
            TimeUnit.MILLISECONDS.toMinutes(selectedDate.timeInMillis - Calendar.getInstance().timeInMillis)

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

    private fun calculateDiscountPercentage(per: Float): Float {
        var calculatedPercentage: Float
        if (per >= 1) {
            calculatedPercentage = per / 100
        } else if (per < 1 && per >= 0.01) {
            calculatedPercentage = per / 10
        } else {
            calculatedPercentage = per
        }
        return calculatedPercentage
    }

    private fun calculateSum(ratePerH: Float, minutes: Long): Float {
        var sum = 0F
        var mins = minutes
        while (mins >= 60) {
            sum += ratePerH
            mins -= 60
        }

        if (mins > 0) {
            sum += mins * ratePerH / 60
        }

        return sum
    }

    private fun isWeekend(): Boolean {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || Calendar.getInstance().get(
            Calendar.DAY_OF_WEEK
        ) == Calendar.SUNDAY
    }

    private fun areRequirementsMet(): Boolean {
        // Selected time should meet requirements (time from now + rent time must be less or equal to selected  date and time)
        return selectedDate.timeInMillis >= (Calendar.getInstance().timeInMillis + MINIMUM_RENT_TIME)
    }

    private fun setFinalRentDateAndCost(tvDate: TextView, tvCost: TextView, totalSum: Float) {
        if (isDateSelected and isTimeSelected) {
            tvDate.text =
                Html.fromHtml(
                    detailCarColorBold(
                        DATE_FORMAT.format(
                            selectedDate.time
                        )
                    )
                )

            tvCost.text =
                Html.fromHtml(
                    detailCarColorBold(
                        String.format("%.2f", totalSum)
                    ) + " " + resources.getString(R.string.nav_header_currency_euro)
                )
        }
    }

    private fun initMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }

        mMap.onCreate(mapViewBundle)

        mMap.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0
        // Disabling map scrolling and other options
        mGoogleMap.uiSettings.setAllGesturesEnabled(false)
        setCameraViewWBounds(mGoogleMap, Constants.LAT_LNG_BOUNDS_OF_LITHUANIA)
        // Getting car data
        loadCar(CAR_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        (activity as MainActivity).enabledWidgets()
        shouldShowHomeButton(activity, true)
        mMap.onResume()
        super.onResume()
    }

    override fun onDetach() {
        shouldShowHomeButton(activity, false)
        // Clearing title of toolbar
        setToolbarTitle("")

        super.onDetach()
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener!!)
        mMap.onStart()
        super.onStart()
    }

    override fun onStop() {
        if (mAuthStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(
            mAuthStateListener!!
        )
        mMap.onStop()
        super.onStop()
    }

    override fun onPause() {
        shouldShowHomeButton(activity, false)
        // Clearing title of toolbar
        setToolbarTitle("")
        mMap.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMap.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mMap.onLowMemory()
        super.onLowMemory()
    }
}
