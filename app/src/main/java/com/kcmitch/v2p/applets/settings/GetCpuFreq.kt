package com.kcmitch.v2p.applets.settings

import java.io.RandomAccessFile
import java.util.Locale

fun getCpuFreq(): String {
    return try {
        val reader = RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r")
        val line = reader.readLine()
        reader.close()
        val freqKhz = line.trim().toLong()
        val freqGhz = freqKhz / 1000000.0
        String.format(Locale.US, "%.2f GHz", freqGhz)
    } catch (e: Exception) {
        try {
            val reader = RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
            val line = reader.readLine()
            reader.close()
            val freqKhz = line.trim().toLong()
            val freqGhz = freqKhz / 1000000.0
            String.format(Locale.US, "%.2f GHz (Max)", freqGhz)
        } catch (ex: Exception) {
            val abis = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown ABI"
            "Scale-on-demand ($abis)"
        }
    }
}
