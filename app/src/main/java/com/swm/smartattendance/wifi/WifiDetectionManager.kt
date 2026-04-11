package com.swm.smartattendance.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * WiFi Hotspot Detection Manager.
 */
class WifiDetectionManager(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _connectedDevices = MutableStateFlow<List<HotspotDevice>>(emptyList())
    val connectedDevices: StateFlow<List<HotspotDevice>> = _connectedDevices.asStateFlow()

    data class HotspotDevice(val ip: String, val mac: String, val deviceName: String = "Unknown")

    /**
     * Scans the ARP table and neighbor cache to find devices connected to the hotspot.
     */
    fun scanConnectedDevices() {
        val devices = mutableMapOf<String, HotspotDevice>()
        
        // Method 1: Try reading /proc/net/arp (Works on older Android)
        try {
            val br = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                val parts = line!!.split(" +".toRegex()).filter { it.isNotBlank() }
                if (parts.size >= 4 && parts[3].matches("..:..:..:..:..:..".toRegex())) {
                    val ip = parts[0]
                    val mac = parts[3].uppercase()
                    if (mac != "00:00:00:00:00:00") {
                        devices[mac] = HotspotDevice(ip, mac)
                    }
                }
            }
            br.close()
        } catch (e: Exception) {
            // Log or handle error
        }

        // Method 2: Try 'ip neigh' command (More reliable on newer Android)
        try {
            val process = Runtime.getRuntime().exec("ip neigh show")
            val reader = BufferedReader(java.io.InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                // Example output: 192.168.43.15 dev wlan0 lladdr a1:b2:c3:d4:e5:f6 REACHABLE
                if (currentLine.contains("lladdr")) {
                    val parts = currentLine.split(" ")
                    val ip = parts[0]
                    val macIdx = parts.indexOf("lladdr") + 1
                    if (macIdx < parts.size) {
                        val mac = parts[macIdx].uppercase()
                        if (mac.matches("..:..:..:..:..:..".toRegex()) && mac != "00:00:00:00:00:00") {
                            devices[mac] = HotspotDevice(ip, mac)
                        }
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            // Log or handle error
        }

        _connectedDevices.value = devices.values.toList()
    }

    /**
     * Check if hotspot is likely enabled.
     * Android does not have a public API to check Tethering state easily without system permissions.
     * We use reflection as a fallback for older versions or just check if WiFi is NOT connected
     * while the app is in "Teacher Mode".
     */
    fun isHotspotEnabled(): Boolean {
        return try {
            val method = wifiManager?.javaClass?.getDeclaredMethod("getWifiApState")
            val state = method?.invoke(wifiManager) as? Int
            // 13 is WIFI_AP_STATE_ENABLED
            state == 13
        } catch (e: Exception) {
            // If reflection fails, we check if WiFi is disabled (hotspot usually disables WiFi client)
            !(wifiManager?.isWifiEnabled ?: false)
        }
    }

    /**
     * Get device's own MAC address
     */
    fun getDeviceMacAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if ("wlan0" == networkInterface.name) {
                    val mac = networkInterface.hardwareAddress ?: continue
                    val buf = StringBuilder()
                    for (b in mac) {
                        buf.append(String.format("%02X:", b))
                    }
                    if (buf.isNotEmpty()) buf.setLength(buf.length - 1)
                    return buf.toString()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun isConnectedToWifi(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun formatMacAddress(mac: String): String {
        val clean = mac.uppercase().replace(Regex("[^A-F0-9]"), "")
        if (clean.length != 12) return mac.uppercase()
        return clean.chunked(2).joinToString(":")
    }

    fun isValidMacAddress(mac: String): Boolean {
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return macRegex.matches(formatMacAddress(mac))
    }
}
