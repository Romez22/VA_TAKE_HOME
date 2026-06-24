package com.virginactive.android.nav

import android.net.Uri
import androidx.annotation.DrawableRes
import com.virginactive.android.R

object Routes {
    const val Login = "login"

    const val Shell = "shell"

    const val Home = "home"
    const val Timetable = "timetable"

    const val ClassDetailArg = "classId"
    const val ClassDetail = "classDetail/{$ClassDetailArg}"

    fun classDetail(classId: String): String = "classDetail/${Uri.encode(classId)}"
}

enum class Tab(val route: String, val label: String, @DrawableRes val icon: Int) {
    HomeTab(Routes.Home, "Home", R.drawable.ic_home),
    ClassesTab(Routes.Timetable, "Classes", R.drawable.ic_classes),
}

val bottomNavTabs: List<Tab> = listOf(Tab.HomeTab, Tab.ClassesTab)
