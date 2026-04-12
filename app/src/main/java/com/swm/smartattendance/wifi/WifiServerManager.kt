package com.swm.smartattendance.wifi

import android.util.Log
import com.google.gson.Gson
import com.swm.smartattendance.model.WifiAttendanceRequest
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class WifiServerManager(port: Int = 8080) : NanoHTTPD(port) {

    private val gson = Gson()
    private val _attendanceFlow = MutableSharedFlow<WifiAttendanceRequest>(extraBufferCapacity = 64)
    val attendanceFlow: SharedFlow<WifiAttendanceRequest> = _attendanceFlow.asSharedFlow()

    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.POST && session.uri == "/attendance") {
            try {
                val contentLength = session.headers["content-length"]?.toInt() ?: 0
                val buffer = ByteArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength) {
                    val read = session.inputStream.read(buffer, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }
                val json = String(buffer)
                
                val request = gson.fromJson(json, WifiAttendanceRequest::class.java)
                if (request != null) {
                    Log.d("WifiServer", "Received attendance: ${request.studentId} from ${request.deviceMac}")
                    _attendanceFlow.tryEmit(request)
                    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"success\"}")
                }
            } catch (e: Exception) {
                Log.e("WifiServer", "Error parsing attendance request", e)
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error: ${e.message}")
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
    }
}
