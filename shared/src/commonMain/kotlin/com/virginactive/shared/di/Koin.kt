package com.virginactive.shared.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(extra: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        extra()
        modules(platformModule(), networkModule(), coreModule())
    }

fun doInitKoin() {
    initKoin()
}
