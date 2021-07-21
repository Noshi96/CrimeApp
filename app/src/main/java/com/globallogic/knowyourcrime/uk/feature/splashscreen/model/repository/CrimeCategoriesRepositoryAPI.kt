package com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository

import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.CrimeCategories
import kotlinx.coroutines.flow.Flow

interface CrimeCategoriesRepositoryAPI {
    fun getCrimeCategories(date: String): Flow<CrimeCategories>
}