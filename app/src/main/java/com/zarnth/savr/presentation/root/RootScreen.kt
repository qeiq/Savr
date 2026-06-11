package com.zarnth.savr.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeScreen
import com.zarnth.savr.presentation.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(viewModel: HomeViewModel = koinViewModel()) {

    //scaffold
    var itemIndex by remember { mutableIntStateOf(0) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surface),
        // bottom app bar
        bottomBar = {
            BottomAppBar {
                bottomAppBarItems.forEachIndexed { index, item ->
                    val isClicked = itemIndex == index
                    NavigationBarItem(
                        label = {
                            Text(item.title)
                        },
                        selected = isClicked,
                        onClick = {
                            itemIndex = index
                        },
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
        },
        // top app bar
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text("Savr Bookmarks")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.homeEvents(HomeEvents.FabClick)
                }
            ) {
                Icon(
                    painterResource(R.drawable.add_icons),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            //HomeScreen
            HomeScreen()
        }
    }

}

