package com.swm.smartattendance.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.NetworkInterface

/**
 * WiFi Hotspot Detection Manager.
 */
class WifiDetectionManager(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _connectedDevices = MutableStateFlow<List<String>>(emptyList())
    val connectedDevices: StateFlow<List<String>> = _connectedDevices.asStateFlow()

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
