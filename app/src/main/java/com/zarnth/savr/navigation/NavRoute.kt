package com.zarnth.savr.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Collections : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data class CollectionDetail(val collectionId: Long) : AppRoute
}
