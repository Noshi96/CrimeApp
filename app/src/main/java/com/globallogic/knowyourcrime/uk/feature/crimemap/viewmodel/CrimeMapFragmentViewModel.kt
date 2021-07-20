package com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.globallogic.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
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
}