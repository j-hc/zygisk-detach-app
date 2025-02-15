package com.jhc.detach.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.jhc.detach.data.model.DetachedApp

@Composable
fun SearchBar(
    detachedApps: MutableState<List<DetachedApp>>,
    searchQuery: MutableState<String>,
    showSystemApps: Boolean,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            searchQuery.value = newText
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search icon"
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear text",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        text = ""
                        searchQuery.value = ""
                        focusManager.clearFocus()
                    }
            )
        },
        label = { Text("Search") },
        shape = RoundedCornerShape(12.dp)
    )
}