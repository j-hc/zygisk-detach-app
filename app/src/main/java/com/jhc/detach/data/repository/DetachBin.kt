package com.jhc.detach.data.repository

import android.content.Context
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream

class DetachBin(filesDir: File, private val context: Context) {
    private val internal = filesDir.resolve("detach.bin").toString()
    private val remote = "/data/adb/modules/zygisk-detach/detach.bin"
    var detached = listOf<String>()

    fun deleteBin() {
        File(internal).delete()
        Shell.cmd("rm -f $remote").exec()
    }

    fun getDetached(dummy: Int = 0): List<String> {
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
            Toast.makeText(context, "ERROR: make sure you flashed zygisk-detach", Toast.LENGTH_LONG)
                .show()
        } else {
            Shell.cmd("am force-stop com.android.vending").exec()
        }
    }
}