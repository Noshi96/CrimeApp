package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

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
import com.google.android.material.chip.Chip
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
                    }
                }
            }
        } ?: run {
            viewModelScope.launch {
                _currentCameraPosition.value?.let { camera ->
                    _currentGPSPosition.value?.let { gps ->
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
        chip: Chip,
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
}





























