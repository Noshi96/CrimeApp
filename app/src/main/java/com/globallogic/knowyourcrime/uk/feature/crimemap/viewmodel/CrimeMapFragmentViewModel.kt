package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globallogic.knowyourcrime.uk.api.CrimesInfoService
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CameraPosition
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CrimeMapFragmentViewModel(
    private val crimesInfoService: CrimesInfoService
) : ViewModel() {

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

    private val _currentCameraPosition = MutableLiveData<CameraPosition>()
    val currentCameraPosition: LiveData<CameraPosition> = _currentCameraPosition

    private val _currentGPSPosition = MutableLiveData<LatLng>()
    val currentGPSPosition: LiveData<LatLng> = _currentGPSPosition

    private val _crimeCategories = MutableLiveData<CrimeCategories>()
    val crimeCategories: LiveData<CrimeCategories> = _crimeCategories

    private val _chipCategories = MutableLiveData<MutableList<String>>()
    val chipCategories: LiveData<MutableList<String>> = _chipCategories

    private val _checkedChipsIdsList = MutableLiveData<List<Int>>()
    var checkedChipsIdsList: LiveData<List<Int>> = _checkedChipsIdsList

    private val _checkedChipsNamesList = MutableLiveData<List<String>>()
    var checkedChipsNamesList: LiveData<List<String>> = _checkedChipsNamesList

    private val _dateFilteredBy = MutableLiveData<String>()
    var dateFilteredBy: LiveData<String> = _dateFilteredBy

    var resetView: Boolean = false

    fun updateCurrentGPSPosition(latitude: Double, longitude: Double) {
        _currentGPSPosition.value = LatLng(latitude, longitude)
    }

    fun updateCurrentCameraPosition(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double
    ) {
        _currentCameraPosition.value = CameraPosition(
            latLngBounds,
            latitude,
            longitude
        )
    }

    fun loadCrimeCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .collect {
                    _crimeCategories.value = it
                }
        }
    }

    fun loadAllCrimes() {
        var categories = _checkedChipsNamesList.value
        if (categories?.isEmpty() == true) {
            categories = null
        }

        categories?.let {
            viewModelScope.launch {
                _currentCameraPosition.value?.let { camera ->
                    _currentGPSPosition.value?.let { gps ->
                        if (_dateFilteredBy.value.isNullOrEmpty()) {
                            crimesInfoService.getRecentCrimesWithCategoriesFromNetwork(
                                categories,
                                camera.latLngBounds,
                                gps.latitude,
                                gps.longitude,
                                camera.latitude,
                                camera.longitude
                            )
                                .collect { crime ->
                                    _allCrimes.value = crime
                                }
                        } else {
                            crimesInfoService.getCrimesWithCategoriesFromNetworkBasesOnNewDate(
                                categories,
                                camera.latLngBounds,
                                gps.latitude,
                                gps.longitude,
                                camera.latitude,
                                camera.longitude,
                                _dateFilteredBy.value!!
                            )
                                .collect { crime ->
                                    _allCrimes.value = crime
                                }
                        }
                    }
                }
            }
        } ?: run {
            viewModelScope.launch {
                _currentCameraPosition.value?.let { camera ->
                    _currentGPSPosition.value?.let { gps ->
                        if (_dateFilteredBy.value.isNullOrEmpty()) {
                            crimesInfoService.getAllRecentCrimesFromNetwork(
                                camera.latLngBounds,
                                gps.latitude,
                                gps.longitude,
                                camera.latitude,
                                camera.longitude
                            )
                                .collect { crime ->
                                    _allCrimes.value = crime
                                }
                        } else {
                            crimesInfoService.getAllCrimesFromNetworkWithDate(
                                camera.latLngBounds,
                                gps.latitude,
                                gps.longitude,
                                camera.latitude,
                                camera.longitude,
                                _dateFilteredBy.value!!
                            )
                                .collect { crime ->
                                    _allCrimes.value = crime
                                }
                        }
                    }
                }
            }
        }

    }

    fun loadChipCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .collect {
                    _chipCategories.value =
                        it.map { category ->
                            category.name
                        }.toMutableList()
                }
        }
    }

    fun getCrimesItemById(id: Int): CrimesItem? =
        _allCrimes.value?.find {
            it.id == id
        }


    fun onSelectedChipChangesSendToViewModel(
        checkedChipIds: List<Int>,
        currentCheckedNames: MutableList<String>
    ) {
        _checkedChipsIdsList.value = checkedChipIds
        _checkedChipsNamesList.value = currentCheckedNames
    }


    fun sortListAlphabetically(isChecked: Boolean) {
        if (isChecked) {
            _allCrimes.value?.sortByDescending { it.category }
        } else {
            _allCrimes.value?.sortBy { it.category }
        }
    }

    fun sortListByDistance(isChecked: Boolean) {
        if (isChecked) {
            _allCrimes.value?.sortByDescending { it.distanceFromGPS }
        } else {
            _allCrimes.value?.sortBy { it.distanceFromGPS }
        }
    }

    fun setDataFilteredBy(data: String) {
        _dateFilteredBy.value = data
    }

    fun clearCheckedChipsNamesList() {
        val newList = mutableListOf<String>()
        _checkedChipsNamesList.value = newList
    }

    fun countCrimes(): StringBuilder {
        val stringBuilder = StringBuilder()

        val newCategories = _crimeCategories.value?.toMutableList()
        val categoryNames = mutableListOf<String>()
        if (newCategories != null) {
            repeat(newCategories.size) { i ->
                _crimeCategories.value?.get(i)?.name?.lowercase()
                    ?.let { categoryNames.add(it.replace(" ", "-")) }
            }
        }
        categoryNames.forEach { crimeCategoriesItem ->
            var count = 0
            if (crimeCategoriesItem != "all-crime") {
                count = _allCrimes.value?.count { crimesItem ->
                    Log.d("${crimesItem.category} ", "${crimeCategoriesItem}")
                    crimesItem.category == crimeCategoriesItem || (crimesItem.category == "violent-crime" && crimeCategoriesItem == "violence-and-sexual-offences")
                            || (crimesItem.category == "criminal-damage-arson" && crimeCategoriesItem == "criminal-damage-and-arson")
                }!!
                stringBuilder.append(
                    "${
                        crimeCategoriesItem.replaceFirstChar {
                            it.uppercase()
                        }.replace('-', ' ')
                    } = $count \n"
                )
            }
        }
        return stringBuilder
    }
}





























