package com.jhc.detach.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppsTabsFilter(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, start = 5.dp, end = 5.dp, top = 0.dp)
    ) {
        listOf("All", "Detached").forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    onTabSelected(index)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(title) }
            )
        }
    }
}