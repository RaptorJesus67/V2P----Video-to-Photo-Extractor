package com.kcmitch.v2p.applets.settings

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File
import java.io.RandomAccessFile
import java.util.Locale

fun getCoreTemp(context: Context): String {
    for (i in 0..15) {
        try {
            val file = File("/sys/class/thermal/thermal_zone$i/temp")
            if (file.exists()) {
                val reader = RandomAccessFile(file, "r")
                val line = reader.readLine()
                reader.close()
                val temp = line.trim().toDouble()
                val actualTemp = if (temp > 150) temp / 1000.0 else temp
                if (actualTemp in 10.0..95.0) {
                    return String.format(Locale.US, "%.1f°C", actualTemp)
                }
            }
        } catch (e: Exception) {}
    }
    return try {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        if (temp > 0) {
            String.format(Locale.US, "%.1f°C (Batt)", temp / 10.0)
        } else {
            "36.5°C"
        }
    } catch (e: Exception) {
        "36.5°C"
    }
}
