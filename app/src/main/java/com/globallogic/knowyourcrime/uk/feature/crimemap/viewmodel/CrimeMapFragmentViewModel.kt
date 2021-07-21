package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.globallogic.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.google.android.material.chip.Chip
import kotlinx.coroutines.*

class CrimeMapFragmentViewModel(private val crimesRepository: CrimesRepositoryAPI) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _allCrimes = MutableLiveData<Crimes>()
    val allCrimes: LiveData<Crimes> = _allCrimes

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var job: Job? = null

    fun getAllCrimes(
        latitude: Double,
        longitude: Double,
        date: String
    ) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = crimesRepository.getAllCrimes(latitude, longitude, date)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _allCrimes.postValue(response.body())
                    _loading.postValue(false)
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }


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

    fun onSelectedChipChangesSendToViewModel(chip: Chip, checkedChipIds: List<Int>, currentCheckedNames: MutableList<String>) {

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