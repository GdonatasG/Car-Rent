package com.android.carrent.fragments.HomeFragment

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.carrent.R
import com.android.carrent.firebase.car.CarListCallback
import com.android.carrent.firebase.car.FirestoreCarRepository
import com.android.carrent.firebase.user.FirestoreUserRepository
import com.android.carrent.models.Car.Car
import com.android.carrent.models.ClusterMarker
import com.android.carrent.models.User.User
import com.android.carrent.utils.ClusterManagerRenderer
import com.android.carrent.utils.constants.FilterConstants
import com.android.carrent.utils.extensions.getAddress
import com.android.carrent.utils.extensions.getDistance
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager

class HomeFragmentViewModel : ViewModel() {
    private val TAG: String = "HomeFragmentViewModel"
    private var userRepository = FirestoreUserRepository()
    private var carRepository = FirestoreCarRepository()
    private var user: MutableLiveData<User> = MutableLiveData()

    // Marker clustering
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var mClusterManagerRenderer: ClusterManagerRenderer

    fun getUser(uid: String): LiveData<User> {
        userRepository.getUser(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "getUser: Listen failed of user.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val u = snapshot.toObject(User::class.java)
                    user.value = u
                } else {
                    Log.d(TAG, "getUser: Snapshot data: null")
                }
            }

        return user
    }

    fun getAllCars(carListCallback: CarListCallback) {
        val carList = mutableListOf<Car>()
        carRepository.getAllCars()
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (doc in it.result!!) {
                        carList.add(doc.toObject(Car::class.java))
                    }
                    carListCallback.onCarListCallback(carList)
                }
            }
    }

    fun handleListUpdates(carList: MutableList<Car>, modifiedCarList: MutableList<Car>) {
        // If modified carList is not empty (user already filtered list), update items of this list
        modifiedCarList.takeIf { it.isNotEmpty() }?.forEachIndexed { i, c ->
            carList.filter { it.id == c.id && it.plateNumber == c.plateNumber }.map { modifiedCarList[i] = it }
        }
    }

    fun filterList(
        modifiedCarList: MutableList<Car>,
        carList: MutableList<Car>,
        filteringArrayCheckedModified: BooleanArray,
        filterText: String,
        context: Context
    ): MutableList<Car> {

        modifiedCarList.clear()
        modifiedCarList.addAll(carList)

        for (i in filteringArrayCheckedModified.indices) {
            when (FilterConstants.filteringArray[i]) {
                FilterConstants.NOT_RENTED -> {
                    if (filteringArrayCheckedModified[i]) modifiedCarList.filter { it.rent.rented!! }.map {
                        modifiedCarList.remove(
                            it
                        )
                    }
                }
                FilterConstants.GAS_TYPE_PETROL -> {
                    if (FilterConstants.filteringArray[i + 1] == FilterConstants.GAS_TYPE_DIESEL) {
                        // Actions should be performed only if one of gas type was unchecked
                        if ((!filteringArrayCheckedModified[i] && filteringArrayCheckedModified[i + 1]) ||
                            (filteringArrayCheckedModified[i] && !filteringArrayCheckedModified[i + 1])
                        ) {
                            // Gas type of petrol is checked, search for cars with diesel and remove it from the list.
                            if (filteringArrayCheckedModified[i]) modifiedCarList.filter {
                                it.model.gasType.equals(
                                    FilterConstants.GAS_TYPE_DIESEL
                                )
                            }.map { modifiedCarList.remove(it) }

                            // Gas type of diesel is checked, search for cars with petrol and remove it from the list.
                            if (filteringArrayCheckedModified[i + 1]) modifiedCarList.filter {
                                it.model.gasType.equals(
                                    FilterConstants.GAS_TYPE_PETROL
                                )
                            }.map { modifiedCarList.remove(it) }
                        } else {
                            filteringArrayCheckedModified[i] = true
                            filteringArrayCheckedModified[i + 1] = true
                        }

                        // Done with gas types. Don`t need to compare it,
                        // because index (i) was gas type of petrol and second index (i+1) gas type of diesel
                        i.plus(1)
                    }
                }
            }
        }
        // Use searchview if user given text is not empty
        if (filterText.isNotEmpty()) {
            // it will proceed if at least one statement fail
            modifiedCarList.filter {
                var city = ""
                var street = ""
                getAddress(
                    lat = it.location!!.latitude,
                    lng = it.location!!.longitude,
                    context = context
                ).locality?.let { city = it }
                getAddress(
                    lat = it.location!!.latitude,
                    lng = it.location!!.longitude,
                    context = context
                ).thoroughfare?.let { street = it }

                !it.model.title!!.toLowerCase().contains(filterText.toLowerCase())
                        && !it.plateNumber!!.toLowerCase().contains(filterText.toLowerCase())
                        && !city.toLowerCase().contains(filterText)
                        && !street.toLowerCase().contains(filterText)
            }.map { modifiedCarList.remove(it) }
        }

        return modifiedCarList
    }

    fun addMapMarkers(map: GoogleMap, list: MutableList<Car>, context: Context?){
        map.let {
            mClusterManager = ClusterManager(context, map)

            mClusterManagerRenderer = ClusterManagerRenderer(context, map, mClusterManager)

            mClusterManager.renderer = mClusterManagerRenderer

            mClusterManager.clearItems()
            map.clear()

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

    fun sortCarList(list: MutableList<Car>, deviceLocation: Location?) {
        list.sortWith(compareBy {
            getDistance(
                deviceLocation!!.latitude,
                deviceLocation.longitude,
                it.location!!.latitude,
                it.location!!.longitude
            )
        })
    }

    fun rvListHandlerOnLocationSuccess(rv: RecyclerView?, pb: ProgressBar?) {
        rv?.visibility = View.VISIBLE
        pb?.visibility = View.GONE
    }

    fun rvListHandlerOnListLoading(rv: RecyclerView?, pb: ProgressBar?) {
        rv?.visibility = View.GONE
        pb?.visibility = View.VISIBLE
    }




}