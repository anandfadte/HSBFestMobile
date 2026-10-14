package org.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

data class EventOut(
    val id: Int,
    val title: String,
    val category: String,
    val description: String? = null,
    @SerializedName("event_date")
    val eventDate: String,
    @SerializedName("event_time")
    val eventTime: String,
    val venue: String,
    val capacity: Int,
    val fee: Int = 0,
    val icon: String? = "✨",
    @SerializedName("available_seats")
    val availableSeats: Int
)

data class EventCreate(
    val title: String,
    val category: String,
    val description: String? = null,
    @SerializedName("event_date")
    val eventDate: String,
    @SerializedName("event_time")
    val eventTime: String,
    val venue: String,
    val capacity: Int,
    val fee: Int = 0,
    val icon: String? = "📅"
)
