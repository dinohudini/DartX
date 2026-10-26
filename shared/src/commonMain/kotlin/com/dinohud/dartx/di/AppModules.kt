package com.dinohud.dartx.di

import com.dinohud.dartx.data.local.AppDatabase
import com.dinohud.dartx.data.repository.MatchRepository
import com.dinohud.dartx.data.repository.PlayerRepository
import com.dinohud.dartx.viewmodel.LiveMatchViewModel
import com.dinohud.dartx.viewmodel.MatchHistoryViewModel
import com.dinohud.dartx.viewmodel.MatchSetupViewModel
import com.dinohud.dartx.viewmodel.MatchStatsViewModel
import com.dinohud.dartx.viewmodel.PlayerStatsViewModel
import com.dinohud.dartx.viewmodel.PlayerViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().matchDao() }
    single { get<AppDatabase>().throwDao() }
    single { PlayerRepository(get()) }
    single { MatchRepository(get(), get()) }
}

val viewModelModule = module {
    viewModel { PlayerViewModel(get(), get()) }
    viewModel { MatchSetupViewModel(get(), get()) }
    viewModel { MatchHistoryViewModel(get(), get()) }
    viewModel { (matchId: Long) -> LiveMatchViewModel(matchId, get(), get()) }
    viewModel { (matchId: Long) -> MatchStatsViewModel(matchId, get(), get()) }
    viewModel { (playerId: Long) -> PlayerStatsViewModel(playerId, get(), get()) }
}

fun initKoin(platformModule: Module, config: KoinApplication.() -> Unit = {}): KoinApplication =
    startKoin {
        config()
        modules(platformModule, dataModule, viewModelModule)
    }
