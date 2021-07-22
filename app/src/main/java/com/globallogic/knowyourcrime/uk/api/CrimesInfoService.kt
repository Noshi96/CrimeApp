package com.globallogic.knowyourcrime.uk.api

import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import com.globallogic.knowyourcrime.uk.feature.crimemap.model.repository.CrimesRepository
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.LastUpdated
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.CrimeCategoriesRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.LastUpdatedRepositoryAPI
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

private const val LOCAL_AREA_ADDITIONAL_OFFSET = 0.005

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

    fun getAllCrimesFromNetwork(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double,
        date: String
    ): Flow<Crimes> = flow {
        val poly = StringBuilder()
        poly.append(latitude)
            .append(",")
            .append(latLngBounds.southwest.longitude - LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(":")
            .append(latLngBounds.southwest.latitude - LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(",")
            .append(latLngBounds.northeast.longitude + LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(":")
            .append(latLngBounds.northeast.latitude + LOCAL_AREA_ADDITIONAL_OFFSET)
            .append(",")
            .append(longitude)

        emitAll(crimesRepository.getAllCrimes(poly.toString(), date))
    }.flowOn(Dispatchers.IO)

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

    fun getAllRecentCrimesFromNetwork(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double
    ): Flow<Crimes> = flow {
        getLastUpdated().collect {
            emitAll(getAllCrimesFromNetwork(latLngBounds, latitude, longitude, cutDate(it.date)))
        }
    }.flowOn(Dispatchers.IO)

    private fun cutDate(date: String): String = date.substring(0, 7)
}