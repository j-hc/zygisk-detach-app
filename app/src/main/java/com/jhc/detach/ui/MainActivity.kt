package com.jhc.detach.ui

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jhc.detach.R
import com.jhc.detach.data.model.DetachedApp
import com.jhc.detach.data.repository.DetachBin
import com.jhc.detach.ui.components.AppsList
import com.jhc.detach.ui.components.AppsTabsFilter
import com.jhc.detach.ui.components.FilterMenu
import com.jhc.detach.ui.components.SearchBar
import com.jhc.detach.ui.theme.ZygiskdetachTheme
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
        if (!Shell.getShell().isRoot) {
            Toast.makeText(applicationContext, "Not root!", Toast.LENGTH_LONG).show()
        }
        val detachBin = DetachBin(filesDir, applicationContext)

        val apps = packageManager.getInstalledPackages(0).mapNotNull { pkgInfo ->
            pkgInfo.applicationInfo?.let { info ->
                val label = packageManager.getApplicationLabel(info).toString()
                val isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                DetachedApp(
                    packageName = pkgInfo.packageName,
                    label = label,
                    isSystemApp = isSystemApp
                )
            }
        }.sortedBy { it.label }.toMutableList()

        val alDetach = try {
            detachBin.getDetached()
        } catch (e: IndexOutOfBoundsException) {
            detachBin.deleteBin()
            emptyList()
        }

        alDetach.forEach { pkgName ->
            if (apps.none { it.packageName == pkgName }) {
                apps.add(
                    DetachedApp(
                        pkgName,
                        pkgName,
                        detached = true,
                        installed = false,
                        isSystemApp = false
                    )
                )
            }
        }

        apps.forEach { it.detached = alDetach.contains(it.packageName) }
        val appsFinal = apps.sortedWith(compareBy({ !it.detached }, { it.label }))

        setContent {
            val detachedApps = remember { mutableStateOf(appsFinal) }
            val searchQuery = remember { mutableStateOf("") }
            val pagerState = rememberPagerState(pageCount = { 2 })
            val coroutineScope = rememberCoroutineScope()
            val baseApps by remember {
                derivedStateOf {
                    if (pagerState.currentPage == 0) {
                        detachedApps.value.filter { !it.detached }
                    } else {
                        detachedApps.value.filter { it.detached }
                    }
                }
            }
            val renderedApps by remember {
                derivedStateOf {
                    if (searchQuery.value.isNotEmpty()) {
                        val searchText = searchQuery.value.lowercase()
                        baseApps.filter { app ->
                            app.packageName.lowercase().contains(searchText) ||
                                    app.label.lowercase().contains(searchText)
                        }
                    } else {
                        baseApps
                    }
                }
            }
            var showSystemApps by remember { mutableStateOf(false) }

            ZygiskdetachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    detachBin.binSerialize()
                                    Toast.makeText(
                                        applicationContext,
                                        if (pagerState.currentPage == 0) "Detached" else "Reattached",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = if (pagerState.currentPage == 0) "Detach" else "Reattach"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (pagerState.currentPage == 0) "Detach" else "Reattach")
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SearchBar(
                                        detachedApps = detachedApps,
                                        searchQuery = searchQuery,
                                        showSystemApps = showSystemApps,
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterMenu(
                                        showSystemApps = showSystemApps,
                                        onShowSystemAppsChange = { showSystemApps = it }
                                    )
                                }

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
                                    AppsList(
                                        packageManager = packageManager,
                                        detachedApps = detachedApps,
                                        renderedApps = renderedApps,
                                        detachBin = detachBin,
                                        uninstalledAppBitmap = getDrawable(R.mipmap.unistalled_app)!!
                                            .toBitmap().asImageBitmap(),
                                        showSystemApps = showSystemApps
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}