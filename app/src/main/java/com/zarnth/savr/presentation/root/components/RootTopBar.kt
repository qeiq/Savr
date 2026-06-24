package com.zarnth.savr.presentation.root.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R
import com.zarnth.savr.presentation.collection.CollectionEvents
import com.zarnth.savr.presentation.collection.CollectionState
import com.zarnth.savr.presentation.collection.CollectionViewModel
import com.zarnth.savr.presentation.home.HomeEvents
import com.zarnth.savr.presentation.home.HomeState
import com.zarnth.savr.presentation.home.HomeViewModel
import com.zarnth.savr.presentation.root.bottomAppBarItems
import com.zarnth.savr.presentation.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    title: String,
    isAllSelected: Boolean,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDelete: () -> Unit,
    onAddToCollection: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.close_icon),
                    contentDescription = "Clear selection"
                )
            }
        },
        actions = {
            if (isAllSelected) {
                IconButton(onClick = onDeselectAll) {
                    Icon(
                        painter = painterResource(R.drawable.deselct_all),
                        contentDescription = "Deselect all",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(onClick = onSelectAll) {
                    Icon(
                        painter = painterResource(R.drawable.selectall_icon),
                        contentDescription = "Select all",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (onAddToCollection != null) {
                IconButton(onClick = onAddToCollection) {
                    Icon(
                        painter = painterResource(R.drawable.bookmark_add),
                        contentDescription = "Add to collection",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.delete_icon),
                    contentDescription = "Delete selected"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    val focusManager = LocalFocusManager.current
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.close_icon),
                    contentDescription = "Close search"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    currentTab: Int,
    showSearchButton: Boolean,
    showSortButton: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                if (currentTab == 0) "Savr Bookmarks"
                else bottomAppBarItems[currentTab].title
            )
        },
        actions = {
            if (showSearchButton) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (showSortButton) {
                IconButton(onClick = onSortClick) {
                    Icon(
                        painter = painterResource(R.drawable.filter_icon),
                        contentDescription = "Sort",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}
