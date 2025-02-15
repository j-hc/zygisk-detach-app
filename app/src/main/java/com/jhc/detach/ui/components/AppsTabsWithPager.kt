package com.jhc.detach.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.jhc.detach.data.model.DetachedApp
import com.jhc.detach.data.repository.DetachBin
import kotlinx.coroutines.launch

@Composable
fun AppsTabsWithPager(
    packageManager: PackageManager,
    detachedApps: MutableState<List<DetachedApp>>,
    detachBin: DetachBin,
    uninstalledAppBitmap: ImageBitmap,
    showSystemApps: Boolean,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val stableAppsAll = remember { mutableStateOf(detachedApps.value.filter { !it.detached }) }
    val stableAppsDetached = remember { mutableStateOf(detachedApps.value.filter { it.detached }) }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            kotlinx.coroutines.delay(50)
            stableAppsAll.value = detachedApps.value.filter { !it.detached }
            stableAppsDetached.value = detachedApps.value.filter { it.detached }
        }
    }

    Column {
        AppsTabsFilter(
            selectedTabIndex = pagerState.currentPage,
            onTabSelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            pagerState = pagerState
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AppsList(
                    packageManager = packageManager,
                    detachedApps = detachedApps,
                    renderedApps = stableAppsAll.value,
                    detachBin = detachBin,
                    uninstalledAppBitmap = uninstalledAppBitmap,
                    showSystemApps = showSystemApps
                )

                1 -> AppsList(
                    packageManager = packageManager,
                    detachedApps = detachedApps,
                    renderedApps = stableAppsAll.value,
                    detachBin = detachBin,
                    uninstalledAppBitmap = uninstalledAppBitmap,
                    showSystemApps = showSystemApps
                )
            }
        }
    }
}