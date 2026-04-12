package com.swm.smartattendance.wifi

import com.google.gson.Gson
import com.swm.smartattendance.model.WifiAttendanceRequest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class WifiClientManager {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun sendAttendance(
        serverIp: String = "192.168.43.1",
        port: Int = 8080,
        studentId: String,
        deviceMac: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val requestBody = WifiAttendanceRequest(studentId, deviceMac)
        val json = gson.toJson(requestBody)
        
        // NanoHTTPD expects postData in the map for parseBody
        // Actually, WifiServerManager.kt uses session.parseBody(map) and then map["postData"]
        // In NanoHTTPD, if it's a POST with JSON, it might be different depending on how parseBody is called.
        // Let's check WifiServerManager.kt again.
        
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$serverIp:$port/attendance")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, "Server error: ${response.code}")
                }
                response.close()
            }
        })
    }
}
