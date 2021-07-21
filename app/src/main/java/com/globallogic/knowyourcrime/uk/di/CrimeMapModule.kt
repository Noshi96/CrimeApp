package com.globallogic.knowyourcrime.uk.di

import com.globallogic.knowyourcrime.uk.feature.crimemap.model.repository.CrimesRepository
import com.globallogic.knowyourcrime.uk.feature.crimemap.viewmodel.CrimeMapFragmentViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val crimeMapModule = module {
    single { CrimesRepository(get()) }

    viewModel { CrimeMapFragmentViewModel(get()) }
}