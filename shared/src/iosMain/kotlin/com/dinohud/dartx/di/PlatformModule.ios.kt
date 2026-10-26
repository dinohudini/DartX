package com.dinohud.dartx.di

import com.dinohud.dartx.data.local.createDartXDatabase
import org.koin.dsl.module

val iosPlatformModule = module {
    single { createDartXDatabase() }
}
