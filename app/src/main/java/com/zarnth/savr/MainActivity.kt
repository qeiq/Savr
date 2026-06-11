package com.zarnth.savr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zarnth.savr.domain.model.Bookmark
import com.zarnth.savr.domain.repository.BookmarkRepository
import com.zarnth.savr.link_fetcher.LinkMetadataParser
import com.zarnth.savr.presentation.root.RootScreen
import com.zarnth.savr.ui.theme.SavrTheme
import com.zarnth.savr.utils.Resource
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavrTheme {
                RootScreen()
            }
        }
    }
}


class UserViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {

    val bookmarks = repository.getBookmarks()

    fun insertBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.insert(bookmark)
        }
    }
}


/*

@Composable
fun HomeScreen(
    viewModel: UserViewModel = koinViewModel()
) {

    val parser = remember { LinkMetadataParser() }

    var urlInput by remember { mutableStateOf("") }

    val bookmarkState by viewModel.bookmarks.collectAsState(
        initial = Resource.Loading()
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // URL Input
        TextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = {
                Text("Enter URL")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Add Button
        Button(
            onClick = {

                if (urlInput.isNotBlank()) {

                    val url = urlInput

                    urlInput = ""

                    scope.launch {

                        val data = parser.parse(url)

                        Log.d("BRO", "Parsed: $data")

                        data?.let {

                            viewModel.insertBookmark(
                                Bookmark(
                                    url = it.url ?: url,
                                    title = it.title,
                                    description = it.description,
                                    imageUrl = it.imageUrl
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Bookmark")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UI State Handling
        when (bookmarkState) {

            is Resource.Loading -> {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {

                val message =
                    (bookmarkState as Resource.Error).errorMessage

                Text(
                    text = message ?: "Unknown Error"
                )
            }

            is Resource.Success -> {

                val bookmarks =
                    (bookmarkState as Resource.Success<List<Bookmark>>).data ?: emptyList()

                LazyColumn {

                    items(bookmarks) { bookmark ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = bookmark.title ?: "No Title",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = bookmark.url,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = bookmark.description
                                        ?: "No Description",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
*/
