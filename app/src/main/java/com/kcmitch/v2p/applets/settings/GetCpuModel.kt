package com.kcmitch.v2p.applets.settings

import java.io.File

fun getCpuModel(): String {
    return try {
        var model = ""
        val file = File("/proc/cpuinfo")
        if (file.exists()) {
            file.forEachLine { line ->
                if (line.startsWith("Hardware", ignoreCase = true) || line.startsWith("Processor", ignoreCase = true) || line.startsWith("model name", ignoreCase = true)) {
                    val parts = line.split(":")
                    if (parts.size > 1) {
                        model = parts[1].trim()
                    }
                }
            }
        }
        if (model.isNotEmpty()) {
            model
        } else {
            val board = android.os.Build.BOARD ?: ""
            val hardware = android.os.Build.HARDWARE ?: ""
            if (hardware.contains("goldfish", ignoreCase = true) || hardware.contains("ranchu", ignoreCase = true)) {
                "Android Virtual CPU"
            } else {
                hardware
            }
        }
    } catch (e: Exception) {
        android.os.Build.HARDWARE ?: "Generic CPU"
    }
}
