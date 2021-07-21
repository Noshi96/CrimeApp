package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globallogic.knowyourcrime.uk.api.CrimesInfoService
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CrimeMapFragmentViewModel(
    private val crimesInfoService: CrimesInfoService
) : ViewModel() {

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

    private val _crimeCategories = MutableLiveData<CrimeCategories>()
    val crimeCategories: LiveData<CrimeCategories> = _crimeCategories

    fun loadCrimeCategories() {
        viewModelScope.launch {
            crimesInfoService.getRecentCrimeCategories()
                .collect {
                    _crimeCategories.value = it
                }
        }
    }

    fun loadAllCrimes(
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            crimesInfoService.getAllRecentCrimesFromNetwork(latitude, longitude)
                .collect {
                    _allCrimes.value = it
                }
        }
    }
}