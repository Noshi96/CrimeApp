package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globallogic.knowyourcrime.uk.api.CrimesInfoService
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CrimeMapFragmentViewModel(
    private val crimesInfoService: CrimesInfoService
) : ViewModel() {

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

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
            onSelectedChipChangeNewDelete.value = true
        }

    }
}