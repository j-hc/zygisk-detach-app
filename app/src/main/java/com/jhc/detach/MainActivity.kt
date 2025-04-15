package com.jhc.detach

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jhc.detach.ui.theme.ZygiskdetachTheme
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream


@Composable
fun AppsList(
    packageManager: PackageManager,
    renderedApps: MutableState<List<DetachedApp>>,
    uninstalledAppBitmap: ImageBitmap
) {

    LazyColumn {
        items(renderedApps.value, key = { it.packageName }) { app ->
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
                        bitmap = if (app.installed) packageManager.getApplicationIcon(app.packageName)
                            .toBitmap()
                            .asImageBitmap() else uninstalledAppBitmap,
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
                        val checkedState = remember { mutableStateOf(app.detached) }
                        Checkbox(
                            checked = checkedState.value, onCheckedChange = {
                                checkedState.value = it
                                app.detached = it
                            }, modifier = Modifier
                                .wrapContentSize()
                                .padding(
                                    PaddingValues(end = 5.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBar(
    detachedApps: MutableState<List<DetachedApp>>, renderedApps: MutableState<List<DetachedApp>>
) {
    var text by remember { mutableStateOf("") }
    val f = LocalFocusManager.current
    OutlinedTextField(
        leadingIcon = {
            Icon(
                Icons.Filled.Search, ""
            )
        },
        trailingIcon = {
            Icon(
                Icons.Filled.Close, "", modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = {
                        text = ""
                        renderedApps.value = detachedApps.value
                        f.clearFocus()
                    })
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, start = 10.dp, end = 10.dp, top = 5.dp),
        value = text,
        onValueChange = {
            text = it
            val searchText = it.lowercase()
            if (it.isNotEmpty()) {
                renderedApps.value = detachedApps.value.filter { app ->
                    app.packageName.lowercase().contains(searchText) || app.label.lowercase()
                        .contains(searchText)
                }
            }
        },
        label = { Text("Search") },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AppsFilter(
    detachedApps: MutableState<List<DetachedApp>>, renderedApps: MutableState<List<DetachedApp>>
) {
    var filterIsDetached by remember { mutableStateOf(false) }
    OutlinedCard(
        border = BorderStroke(0.5.dp, if (filterIsDetached) Color.Cyan else Color.Gray),
        modifier = Modifier.padding(start = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .clickable(onClick = {
                    filterIsDetached = !filterIsDetached
                    if (filterIsDetached) {
                        renderedApps.value = detachedApps.value.filter { app -> app.detached }
                    } else {
                        renderedApps.value = detachedApps.value
                    }
                }),

            ) {
            Text(
                "Detached", modifier = Modifier.padding(10.dp), fontSize = 13.sp
            )
        }
    }
}

data class DetachedApp(
    val packageName: String,
    val label: String,
    var detached: Boolean = false,
    val installed: Boolean = true
)

class Toaster(private val context: Context) {
    fun toast(msg: String, delay: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, delay).show()
    }
}

fun runShell(cmd: String, toaster: Toaster): String {
    val op = Shell.cmd(cmd).exec()
    if (op.code != 0) {
        toaster.toast("ERROR: " + op.getErr().joinToString("\n"), Toast.LENGTH_LONG)
        return ""
    } else {
        return op.getOut().joinToString("\n")
    }
}

fun String.splitn() =
    if (isEmpty()) emptyList()
    else this.split('\n')

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toaster = Toaster(applicationContext)
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
        if (!Shell.getShell().isRoot) {
            toaster.toast("Not root", Toast.LENGTH_SHORT)
            finishAndRemoveTask()
            return
        }
        if (Shell.cmd("test -f /data/adb/modules/zygisk-detach/detach").exec().code != 0) {
            toaster.toast("zygisk-detach is not installed", Toast.LENGTH_SHORT)
            finishAndRemoveTask()
            return
        }
        var apps = packageManager.getInstalledPackages(0).map {
            DetachedApp(
                it.packageName, packageManager.getApplicationLabel(it.applicationInfo).toString()
            )
        }.sortedBy { it.label }.toMutableList()
        val alDetach: List<String> = try {
            runShell("/data/adb/modules/zygisk-detach/detach list", toaster).splitn()
        } catch (e: IndexOutOfBoundsException) {
            runShell("/data/adb/modules/zygisk-detach/detach reset", toaster)
            listOf()
        }
        for (d in alDetach) {
            if (apps.find { it.packageName == d } == null) {
                apps.add(DetachedApp(d, d, detached = true, installed = false))
            }
        }
        apps.forEach { it.detached = alDetach.contains(it.packageName) }
        val appsFinal = apps.sortedBy { it.label }.sortedBy { !it.detached }.toList()
        setContent {
            val detachedApps = remember { mutableStateOf(appsFinal) }
            val renderedApps = remember { mutableStateOf(appsFinal) }
            ZygiskdetachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {}, floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val detachedList = detachedApps.value.filter { app -> app.detached }
                                    .map { app -> app.packageName }
                            if (detachedList.isEmpty()) {
                                runShell("/data/adb/modules/zygisk-detach/detach reset", toaster)
                                toaster.toast("Emptied the detach list")
                            } else {
                                val detachStr = detachedList.joinToString(" ")
                                runShell("/data/adb/modules/zygisk-detach/detach detachall $detachStr", toaster)
                                toaster.toast("Detached")
                            }
                        }, modifier = Modifier) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Check,
                                    "Detach",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text("Detach", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }, content = {
                        Box(modifier = Modifier.padding(3.dp)) {
                            Column {
                                SearchBar(detachedApps, renderedApps)
                                AppsFilter(detachedApps, renderedApps)
                                AppsList(
                                    packageManager = packageManager,
                                    renderedApps = renderedApps,
                                    getDrawable(R.mipmap.unistalled_app)!!.toBitmap().asImageBitmap()
                                )
                            }
                        }
                    })
                }
            }
        }
    }
}
