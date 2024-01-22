package com.jhc.detach

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
    detachedApps: MutableState<List<DetachedApp>>,
    renderedApps: MutableState<List<DetachedApp>>,
    detachBin: DetachBin
) {
    LazyColumn {
        items(renderedApps.value, key = { it.pi.packageName }) { app ->
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
                        bitmap = packageManager.getApplicationIcon(app.pi.packageName).toBitmap()
                            .asImageBitmap(),
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
                                text = app.pi.packageName,
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
                                detachBin.detached =
                                    detachedApps.value.filter { app -> app.detached }
                                        .map { app -> app.pi.packageName }
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
                    app.pi.packageName.lowercase().contains(searchText) || app.label.lowercase()
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
    val pi: PackageInfo, val label: String, var detached: Boolean = false
)

class DetachBin(filesDir: File, private val context: Context) {
    private val internal = filesDir.resolve("detach.bin").toString()
    private val remote = "/data/adb/modules/zygisk-detach/detach.bin"
    var detached = listOf<String>()

    fun getDetached(a: Int = 0): List<String> {
        File(internal).delete()
        if (Shell.cmd("cp -f $remote $internal").exec().code == 0) {
            Shell.cmd("chmod 0777 $internal").exec()
        }
        val detachTxt = try {
            context.openFileInput("detach.bin").readBytes()
        } catch (_: Exception) {
            return listOf()
        }
        var i = 0
        val detached = mutableListOf<String>()
        while (i < detachTxt.size) {
            val len: Byte = detachTxt[i]
            i += 1
            val encodedName = detachTxt.sliceArray(i until i + len.toInt())
            val name =
                String(encodedName.filterIndexed { index, _ -> index % 2 == 0 }.toByteArray())
            detached.add(name)
            i += len.toInt()
        }
        return detached
    }

    fun binSerialize() {
        val fileOutputStream = FileOutputStream(internal, false).buffered()
        for (app in detached) {
            val w = mutableListOf<Byte>()
            for (b in app.substring(0, app.length - 1).toByteArray()) {
                w.add(b)
                w.add(0)
            }
            w.add(app.toByteArray()[app.length - 1])
            fileOutputStream.write(byteArrayOf(w.size.toByte()))
            fileOutputStream.write(w.toByteArray())
        }
        fileOutputStream.flush()
        fileOutputStream.close()
        val r = Shell.cmd("cp -f $internal $remote").exec()
        if (r.code != 0) {
            Toast.makeText(context, "ERROR", Toast.LENGTH_LONG).show()
        }
        Shell.cmd("am force-stop com.android.vending").exec()
    }
}

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER))
        if (!Shell.getShell().isRoot) {
            Toast.makeText(applicationContext, "Not root!", Toast.LENGTH_LONG).show()
        }
        val detachBin = DetachBin(filesDir, applicationContext)

        var apps = packageManager.getInstalledPackages(0).map {
            DetachedApp(
                it, packageManager.getApplicationLabel(it.applicationInfo).toString()
            )
        }.sortedBy { it.label }
        val alDetach = detachBin.getDetached()
        apps.forEach { it.detached = alDetach.contains(it.pi.packageName) }
        apps = apps.sortedBy { it.label }.sortedBy { !it.detached }
        setContent {
            val detachedApps = remember { mutableStateOf(apps) }
            val renderedApps = remember { mutableStateOf(apps) }
            ZygiskdetachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {}, floatingActionButton = {
                        FloatingActionButton(onClick = {
                            detachBin.binSerialize()
                            Toast.makeText(applicationContext, "Detached", Toast.LENGTH_SHORT)
                                .show()
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
                                    detachedApps = detachedApps,
                                    renderedApps = renderedApps,
                                    detachBin = detachBin
                                )
                            }
                        }
                    })
                }
            }
        }
    }
}
