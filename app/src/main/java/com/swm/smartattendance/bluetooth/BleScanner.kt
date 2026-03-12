package com.swm.smartattendance.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BLE Scanner for proximity-based attendance.
 * Scans for nearby BLE devices and extracts their identifiers.
 */
class BleScanner(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val _scanResults = MutableStateFlow<List<BleDeviceInfo>>(emptyList())
    val scanResults: StateFlow<List<BleDeviceInfo>> = _scanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var scanTimeoutRunnable: Runnable? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val bleId = device.address ?: device.name ?: "unknown"
            val deviceInfo = BleDeviceInfo(
                address = device.address,
                name = device.name ?: "Unknown",
                rssi = result.rssi,
                bleId = bleId
            )
            updateScanResult(deviceInfo)
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    /**
     * Data class for BLE device information
     */
    data class BleDeviceInfo(
        val address: String,
        val name: String,
        val rssi: Int,
        val bleId: String
    )

    /**
     * Check if Bluetooth is available and enabled
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /**
     * Start BLE scan for nearby devices
     * @param scanDurationMs Duration to scan in milliseconds (default 10 seconds)
     */
    @SuppressLint("MissingPermission")
    fun startScan(scanDurationMs: Long = 10000L) {
        if (!isBluetoothEnabled()) return
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        _scanResults.value = emptyList()
        _isScanning.value = true

        try {
            bluetoothLeScanner?.startScan(scanCallback)
            scanTimeoutRunnable = Runnable { stopScan() }
            handler.postDelayed(scanTimeoutRunnable!!, scanDurationMs)
        } catch (e: SecurityException) {
            _isScanning.value = false
        }
    }

    /**
     * Stop BLE scan
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanTimeoutRunnable?.let { handler.removeCallbacks(it) }
        scanTimeoutRunnable = null
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            // Ignore
        }
        _isScanning.value = false
    }

    private fun updateScanResult(deviceInfo: BleDeviceInfo) {
        val current = _scanResults.value.toMutableList()
        val index = current.indexOfFirst { it.address == deviceInfo.address }
        if (index >= 0) {
            current[index] = deviceInfo
        } else {
            current.add(deviceInfo)
        }
        _scanResults.value = current
    }

    /**
     * Get list of BLE IDs from scanned devices
     */
    fun getScannedBleIds(): List<String> = _scanResults.value.map { it.bleId }
}
