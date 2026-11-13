package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface RyujinRoute {
    @Serializable data object Home : RyujinRoute
    @Serializable data object Chapters : RyujinRoute
    @Serializable data object Characters : RyujinRoute
    @Serializable data object Community : RyujinRoute
    @Serializable data object Studio : RyujinRoute
    @Serializable data class Reader(val novelId: String, val chapterNumber: Int) : RyujinRoute
}
