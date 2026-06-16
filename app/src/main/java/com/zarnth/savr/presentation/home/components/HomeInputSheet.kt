package com.zarnth.savr.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zarnth.savr.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInputSheet(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit,
    value: String,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "New Bookmark",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                val hasScheme = value.startsWith("http://") || value.startsWith("https://")
                val looksLikeDomain = value.contains(".") && !value.contains(" ") && value.isNotBlank()
                val isValidUrl = hasScheme || looksLikeDomain
                val showError = value.isNotBlank() && !isValidUrl

                OutlinedTextField(
                    value = value,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError,
                    leadingIcon = {
                        Icon(
                           painter = painterResource(R.drawable.link_three),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    placeholder = {
                        Text("https://example.com")
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    supportingText = if (showError) {
                        {
                            Text(
                                "Enter a valid URL",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSaveClick()
                        onDismissRequest()
                    },
                    enabled = isValidUrl,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Bookmark")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}