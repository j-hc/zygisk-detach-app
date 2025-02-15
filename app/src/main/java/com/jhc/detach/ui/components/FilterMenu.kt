package com.jhc.detach.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FilterMenu(
    showSystemApps: Boolean,
    onShowSystemAppsChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Filter Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text("Show system apps")
                },
                trailingIcon = {
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { onShowSystemAppsChange(it) }
                    )
                },
                onClick = {
                    onShowSystemAppsChange(!showSystemApps)
                    expanded = false
                }
            )
        }
    }
}