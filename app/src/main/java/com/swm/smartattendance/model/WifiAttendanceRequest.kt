package com.swm.smartattendance.model

import com.google.gson.annotations.SerializedName

data class WifiAttendanceRequest(
    @SerializedName("student_id")
    val studentId: String,
    @SerializedName("device_mac")
    val deviceMac: String
)
