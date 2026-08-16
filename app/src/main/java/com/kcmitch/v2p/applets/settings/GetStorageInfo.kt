package com.kcmitch.v2p.applets.settings

import android.os.Environment
import android.os.StatFs
import java.util.Locale

fun getStorageInfo(): String {
    return try {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
        val freeGb = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
        String.format(Locale.US, "%.1f / %.1f GB Free", freeGb, totalGb)
    } catch (e: Exception) {
        "N/A"
    }
}
