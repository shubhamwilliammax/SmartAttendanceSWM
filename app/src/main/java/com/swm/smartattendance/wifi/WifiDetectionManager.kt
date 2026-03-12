package com.swm.smartattendance.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.NetworkInterface

/**
 * WiFi Hotspot Detection Manager.
 * Detects devices connected to teacher's hotspot using MAC address.
 * Note: Android 10+ restricts MAC address access - this provides best-effort detection.
 */
class WifiDetectionManager(private val context: Context) {

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectedDevices = MutableStateFlow<List<String>>(emptyList())
    val connectedDevices: StateFlow<List<String>> = _connectedDevices.asStateFlow()

    /**
     * Get device's own MAC address (for student device identification)
     * Returns null on Android 10+ due to privacy restrictions
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

    /**
     * Check if device is connected to WiFi
     */
    fun isConnectedToWifi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Get WiFi SSID when connected (for teacher hotspot name)
     */
    @Suppress("DEPRECATION")
    fun getConnectedWifiSsid(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val currentNetwork = connectivityManager.activeNetwork ?: return null
                val wifiInfo = connectivityManager.getNetworkCapabilities(currentNetwork)
                // SSID is not directly available in NetworkCapabilities
                wifiManager?.connectionInfo?.ssid?.trim('"')
            } else {
                wifiManager?.connectionInfo?.ssid?.trim('"')
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Refresh connected devices list.
     * On Android, getting list of connected devices to a hotspot requires
     * root access or being the hotspot owner. This method provides a placeholder
     * for when such API becomes available. For now, students can register their
     * MAC address in the app and teacher can verify connection status.
     */
    fun refreshConnectedDevices() {
        // Android doesn't provide API to get connected hotspot clients without root
        // This would require a server-side solution or manufacturer-specific APIs
        // For demo: return empty list - in production, use a backend service
        _connectedDevices.value = emptyList()
    }

    /**
     * Format MAC address for display (e.g., AA:BB:CC:DD:EE:FF)
     */
    fun formatMacAddress(mac: String): String {
        return mac.uppercase().replace("-", ":").replace(" ", "")
    }

    /**
     * Validate MAC address format
     */
    fun isValidMacAddress(mac: String): Boolean {
        val macRegex = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$".toRegex()
        return macRegex.matches(formatMacAddress(mac))
    }
}
