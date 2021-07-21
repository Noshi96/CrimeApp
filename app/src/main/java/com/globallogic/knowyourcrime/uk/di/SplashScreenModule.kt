package com.globallogic.knowyourcrime.uk.di

import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.CrimeCategoriesRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.LastUpdatedRepositoryAPI
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.LocalCrimeCategoriesRepository
import com.globallogic.knowyourcrime.uk.feature.splashscreen.model.repository.LocalLastUpdatedRepository
import com.globallogic.knowyourcrime.uk.feature.splashscreen.viewmodel.SplashScreenViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val splashScreenModule = module {
    single<CrimeCategoriesRepositoryAPI> {
        LocalCrimeCategoriesRepository(get())
    }

    single<LastUpdatedRepositoryAPI> {
        LocalLastUpdatedRepository(get())
    }

    viewModel {
        SplashScreenViewModel(get())
    }
}