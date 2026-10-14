package org.zenith26.fest.data.api

import org.zenith26.fest.data.model.EventCreate
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.data.model.RegistrationIn
import org.zenith26.fest.data.model.RegistrationItemOut
import org.zenith26.fest.data.model.RegistrationOut
import org.zenith26.fest.data.model.StatsOut
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ZenithApiService {

    @GET("api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): List<EventOut>

    @GET("api/events/{event_id}")
    suspend fun getEventDetails(
        @Path("event_id") eventId: Int
    ): EventOut

    @POST("api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body payload: RegistrationIn
    ): Response<RegistrationOut>

    @GET("api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): List<RegistrationItemOut>

    @GET("api/stats")
    suspend fun getStats(): StatsOut

    @POST("api/events")
    suspend fun createEvent(
        @Body payload: EventCreate
    ): Response<EventOut>

    @DELETE("api/registrations/{registration_id}")
    suspend fun cancelRegistration(
        @Path("registration_id") registrationId: Int
    ): Response<Map<String, String>>
}
