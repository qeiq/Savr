package com.zarnth.savr.presentation.root

import com.zarnth.savr.R

data class BottomAppBarItem(
    val title: String,
    val icon: Int,
    val iconFilled: Int
)

val bottomAppBarItems = listOf<BottomAppBarItem>(
    BottomAppBarItem(
        title = "Home",
        icon = R.drawable.home_icon,
        iconFilled = R.drawable.home_icon_filled
    ),
    BottomAppBarItem(
        title = "Search",
        icon = R.drawable.search_icon,
        iconFilled = R.drawable.search_icon
    ),
    BottomAppBarItem(
        title = "Settings",
        icon = R.drawable.setting_icon,
        iconFilled = R.drawable.setting_icon_filled
    ),
)
