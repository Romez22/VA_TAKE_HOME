package com.virginactive.android.di

import com.virginactive.android.ui.detail.ClassDetailViewModel
import com.virginactive.android.ui.home.HomeViewModel
import com.virginactive.android.ui.login.LoginViewModel
import com.virginactive.android.ui.timetable.TimetableViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::TimetableViewModel)
    viewModelOf(::ClassDetailViewModel)
}
