package com.zarnth.savr.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Search : AppRoute
    @Serializable data object Settings : AppRoute
}
