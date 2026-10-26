package com.dinohud.dartx.di

import com.dinohud.dartx.data.local.createDartXDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single { createDartXDatabase(androidContext()) }
}
