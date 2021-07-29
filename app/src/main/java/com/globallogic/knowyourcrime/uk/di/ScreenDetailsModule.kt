package com.globallogic.knowyourcrime.uk.di

import com.globallogic.knowyourcrime.uk.feature.detailsscreen.viewmodel.ScreenDetailsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val screenDetailsModule = module {
    viewModel { ScreenDetailsViewModel() }
}