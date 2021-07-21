package com.globallogic.knowyourcrime.uk.api

import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.repository.CrimesRepository
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.CrimeCategoriesRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.LastUpdatedRepositoryAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class CrimesInfoService(
    private val crimeCategoriesRepository: CrimeCategoriesRepositoryAPI,
    private val lastUpdatedRepository: LastUpdatedRepositoryAPI,
    private val crimesRepository: CrimesRepository
) {

    fun getLastUpdated(): Flow<LastUpdated> = lastUpdatedRepository.getLastUpdated()
    fun getCrimeCategories(date: String): Flow<CrimeCategories> =
        crimeCategoriesRepository.getCrimeCategories(date)

    fun getAllCrimesFromNetwork(latitude: Double, longitude: Double, date: String): Flow<Crimes> =
        crimesRepository.getAllCrimes(latitude, longitude, date)

    fun getRecentCrimeCategories(): Flow<CrimeCategories> = flow {
        getLastUpdated().collect {
            emitAll(getCrimeCategories(cutDate(it.date)))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllRecentCrimesFromNetwork(latitude: Double, longitude: Double): Flow<Crimes> = flow {
        getLastUpdated().collect {
            emitAll(getAllCrimesFromNetwork(latitude, longitude, cutDate(it.date)))
        }
    }.flowOn(Dispatchers.IO)

    private fun cutDate(date: String): String = date.substring(0, 7)
}