package com.jhc.detach.data.model

import android.graphics.Bitmap

data class DetachedApp(
    val packageName: String,
    val label: String,
    var detached: Boolean = false,
    val installed: Boolean = true,
    val isSystemApp: Boolean,
    val icon: Bitmap? = null
)