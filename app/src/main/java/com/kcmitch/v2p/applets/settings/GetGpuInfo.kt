package com.kcmitch.v2p.applets.settings

fun getGpuInfo(): String {
    return try {
        val board = android.os.Build.BOARD ?: ""
        val hardware = android.os.Build.HARDWARE ?: ""
        if (hardware.contains("goldfish", ignoreCase = true) || hardware.contains("ranchu", ignoreCase = true)) {
            "Android Emulator GPU (SwiftShader)"
        } else if (board.isNotEmpty() || hardware.isNotEmpty()) {
            "$board ($hardware)"
        } else {
            "Mali/Adreno Graphics"
        }
    } catch (e: Exception) {
        "Generic GPU"
    }
}
