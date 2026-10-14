package org.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class RegistrationIn(
    @SerializedName("student_name")
    val studentName: String,
    @SerializedName("roll_number")
    val rollNumber: String,
    val email: String,
    val phone: String? = null
)

data class RegistrationOut(
    val id: Int,
    @SerializedName("event_id")
    val eventId: Int,
    @SerializedName("student_name")
    val studentName: String,
    @SerializedName("registered_at")
    val registeredAt: String
)

data class RegistrationItemOut(
    val id: Int,
    @SerializedName("event_id")
    val eventId: Int,
    @SerializedName("student_name")
    val studentName: String,
    @SerializedName("roll_number")
    val rollNumber: String,
    val email: String,
    val phone: String? = null,
    @SerializedName("registered_at")
    val registeredAt: String,
    @SerializedName("event_title")
    val eventTitle: String
)
