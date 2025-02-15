package com.jhc.detach.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhc.detach.data.model.DetachedApp
import com.jhc.detach.data.repository.DetachBin
import android.content.pm.PackageManager
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppsList(
    packageManager: PackageManager,
    detachedApps: MutableState<List<DetachedApp>>,
    renderedApps: List<DetachedApp>,
    detachBin: DetachBin,
    uninstalledAppBitmap: ImageBitmap,
    showSystemApps: Boolean,
) {
    val filteredApps by remember(renderedApps, showSystemApps) {
        derivedStateOf {
            if (showSystemApps) renderedApps else renderedApps.filter { !it.isSystemApp }
        }
    }

    val iconCache = remember { mutableMapOf<String, ImageBitmap>() }

    if (filteredApps.isEmpty()) {
        Text(
            text = "You don't have any detached applications.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val bitmap = iconCache.getOrPut(app.packageName) {
                    if (app.installed) {
                        val drawable = packageManager.getApplicationIcon(app.packageName)
                        drawable.toBitmap(128, 128).asImageBitmap()
                    } else {
                        uninstalledAppBitmap
                    }
                }

                OutlinedCard(
                    modifier = Modifier.padding(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(85.dp)
                            .padding(3.dp)
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxHeight(0.6f)
                                .padding(5.dp)
                                .padding(PaddingValues(start = 5.dp, end = 10.dp)),
                            bitmap = bitmap,
                            contentDescription = ""
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = app.label, modifier = Modifier.wrapContentHeight())
                                Text(
                                    maxLines = 1,
                                    text = app.packageName,
                                    fontSize = 14.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .alpha(0.8f)
                                )
                            }
                            Checkbox(
                                checked = app.detached,
                                onCheckedChange = { isChecked ->
                                    app.detached = isChecked
                                    detachBin.detached =
                                        detachedApps.value.filter { it.detached }
                                            .map { it.packageName }
                                },
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(PaddingValues(end = 5.dp))
                            )
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "- End -", color = Color.Gray)
                }
            }
        }
    }
}