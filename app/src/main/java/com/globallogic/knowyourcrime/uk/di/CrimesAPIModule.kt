package com.globallogic.knowyourcrime.uk.di

import com.globallogic.knowyourcrime.uk.api.CrimesRepositoryAPI
import com.globallogic.knowyourcrime.uk.api.CrimesRepositoryAPIFactory
import org.koin.dsl.module
import retrofit2.Retrofit

val crimesAPIModule = module {
    single {
        CrimesRepositoryAPIFactory().getRetrofit()
    }

    single {
        get<Retrofit>().create(CrimesRepositoryAPI::class.java)
    }
}