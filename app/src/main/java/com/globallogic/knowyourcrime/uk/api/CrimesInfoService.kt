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

private const val LOCAL_AREA_ADDITIONAL_OFFSET = 0.006
private const val OFFLINE_LAST_UPDATED = "2021-05"

class CrimesInfoService(
    private val crimeCategoriesRepository: CrimeCategoriesRepositoryAPI,
    private val lastUpdatedRepository: LastUpdatedRepositoryAPI,
    private val crimesRepository: CrimesRepository
) {

    fun getLastUpdated(): Flow<LastUpdated> = try {
        lastUpdatedRepository.getLastUpdated()
    } catch (exception: Exception) {
        flowOf(LastUpdated(OFFLINE_LAST_UPDATED))
    }


    fun getCrimeCategories(date: String): Flow<CrimeCategories> = try {
        crimeCategoriesRepository.getCrimeCategories(date)
    } catch (exception: Exception) {
        flowOf(CrimeCategories())
    }

    fun getAllCrimesFromNetwork(latitude: Double, longitude: Double, date: String): Flow<Crimes> =
        try {
            crimesRepository.getAllCrimes(latitude, longitude, date)
        } catch (exception: Exception) {
            flowOf(Crimes())
        }

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

        try {
            emitAll(crimesRepository.getAllCrimes(poly.toString(), date))
        } catch (exception: Exception) {
            emitAll(flowOf(Crimes()))
        }
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

    fun getRecentCrimesWithCategoriesFromNetwork(
        categories: List<String>,
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double
    ): Flow<Crimes> = flow {

        val newCategories = categories.toMutableList()
        repeat(newCategories.size) { i ->
            newCategories[i] = categories[i].lowercase().replace(" ", "-")
        }

        getAllRecentCrimesFromNetwork(latLngBounds, latitude, longitude).collect {
            val foundCrimes = Crimes()
            it.forEach { crime ->
                if (newCategories.contains(crime.category)) {
                    foundCrimes.add(crime)
                }
            }
            emit(foundCrimes)
        }
    }.flowOn(Dispatchers.IO)

    private fun cutDate(date: String): String = date.substring(0, 7)

    fun getCrimesWithCategoriesFromNetworkBasesOnNewDate(
        categories: List<String>,
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double,
        date: String
    ): Flow<Crimes> = flow {

        val newCategories = categories.toMutableList()
        repeat(newCategories.size) { i ->
            newCategories[i] = categories[i].lowercase().replace(" ", "-")
        }

        getAllCrimesFromNetworkBasedOnDate(latLngBounds, latitude, longitude,date).collect {
            val foundCrimes = Crimes()
            it.forEach { crime ->
                if (newCategories.contains(crime.category)) {
                    foundCrimes.add(crime)
                }
            }
            emit(foundCrimes)
        }
    }.flowOn(Dispatchers.IO)

    fun getAllCrimesFromNetworkBasedOnDate(
        latLngBounds: LatLngBounds,
        latitude: Double,
        longitude: Double,
        data: String
    ): Flow<Crimes> = flow {
        emitAll(getAllCrimesFromNetwork(latLngBounds, latitude, longitude, data))
    }.flowOn(Dispatchers.IO)


}