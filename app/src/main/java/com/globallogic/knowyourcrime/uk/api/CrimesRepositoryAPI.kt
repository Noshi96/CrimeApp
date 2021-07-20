package com.globallogic.knowyourcrime.uk.api

import com.globallogic.knowyourcrime.uk.feature.crimemap.model.Crimes
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CrimesRepositoryAPI {
    @GET("/api/crimes-street/all-crime")
    suspend fun getAllCrimes(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("date") date: String
    ): Response<Crimes>
}