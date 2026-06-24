package com.zarnth.savr.presentation.root.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zarnth.savr.presentation.root.bottomAppBarItems

@Composable
fun RootBottomBar(
    currentTab: Int,
    onTabChange: (Int) -> Unit
) {
    BottomAppBar {
        bottomAppBarItems.forEachIndexed { index, item ->
            val isClicked = currentTab == index
            NavigationBarItem(
                label = { Text(item.title) },
                selected = isClicked,
                onClick = { onTabChange(index) },
                icon = {
                    Icon(
                        painter = painterResource(if (isClicked) item.iconFilled else item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}
