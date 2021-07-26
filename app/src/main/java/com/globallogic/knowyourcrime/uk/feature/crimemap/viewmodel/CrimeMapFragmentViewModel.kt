package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globallogic.knowyourcrime.uk.api.CrimesInfoService
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.CrimesItem
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrimeMapFragmentViewModel(
    private val crimesInfoService: CrimesInfoService
) : ViewModel() {

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

    private val _currentCrimesToDisplay = MutableLiveData<Crimes>()
    val currentCrimesToDisplay: LiveData<Crimes> = _currentCrimesToDisplay

    private val _crimeCategories = MutableLiveData<CrimeCategories>()
    val crimeCategories: LiveData<CrimeCategories> = _crimeCategories

    private val _chipCategories = MutableLiveData<MutableList<String>>()
    val chipCategories: LiveData<MutableList<String>> = _chipCategories

    private val _currentCheckedChipName = MutableLiveData<String>()
    var currentCheckedChipName: LiveData<String> = _currentCheckedChipName

    private val _currentCheckedChipId = MutableLiveData<Int>()
    var currentCheckedChipId: LiveData<Int> = _currentCheckedChipId

    private val _checkedChipsIdsList = MutableLiveData<List<Int>>()
    var checkedChipsIdsList: LiveData<List<Int>> = _checkedChipsIdsList

    private val _checkedChipsNamesList = MutableLiveData<List<String>>()
    var checkedChipsNamesList: LiveData<List<String>> = _checkedChipsNamesList

    var onSelectedChipChangeNewAdd = MutableLiveData<Boolean?>()
    var onSelectedChipChangeNewDelete = MutableLiveData<Boolean?>()

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
                    loadListFilteredByChipsNames()
                }
        }
    }

    fun loadAllCrimes(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            crimesInfoService.getAllRecentCrimesFromNetwork(latLngBounds, latitude, longitude)
                .collect {
                    _allCrimes.value = it
                    loadListFilteredByChipsNames()
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
        _currentCheckedChipName.value = chip.text.toString()
        _currentCheckedChipId.value = chip.id

        if (chip.isChecked) {
            onSelectedChipChangeNewAdd.value = true
        } else {
            onSelectedChipChangeNewDelete.value = false
        }
    }

    fun loadListFilteredByChipsNames() {
        val newCrimesList = Crimes()
        viewModelScope.launch {
            checkedChipsNamesList.value?.forEach { checkedChipsName ->
                allCrimes.value?.forEach { crimeItem ->
                    if (checkedChipsName == crimeItem.category.replaceFirstChar {
                            it.uppercase()
                        }.replace('-', ' ')) {
                        newCrimesList.add(crimeItem)
                    } else if (checkedChipsName.replace(
                            '-', ' '
                        ) == crimeItem.category.replaceFirstChar {
                            it.uppercase()
                        }.replace('-', ' ')
                    ) {
                        newCrimesList.add(crimeItem)
                    } else if (checkedChipsName == "Criminal damage and arson" && crimeItem.category == "criminal-damage-arson") {
                        newCrimesList.add(crimeItem)
                    } else if (checkedChipsName == "Violence and sexual offences" && crimeItem.category == "violent-crime") {
                        newCrimesList.add(crimeItem)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (_currentCrimesToDisplay.value?.size == 0 || newCrimesList.size == 0){
                    _currentCrimesToDisplay.value = _allCrimes.value
                } else {
                    _currentCrimesToDisplay.value = newCrimesList
                }
            }
        }
    }

    fun getCurrentLocation(){

    }

    fun sortListAlphabetically(isChecked: Boolean){
        if(isChecked){
            _currentCrimesToDisplay.value?.sortByDescending { it.category }
        } else {
            _currentCrimesToDisplay.value?.sortBy { it.category }
        }

    }

}





























