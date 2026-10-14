package org.zenith26.fest.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zenith26.fest.data.api.NetworkModule
import org.zenith26.fest.data.model.EventCreate
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.data.model.RegistrationIn
import org.zenith26.fest.data.model.RegistrationItemOut
import org.zenith26.fest.data.model.RegistrationOut
import org.zenith26.fest.data.model.StatsOut

class ZenithRepository {

    private val api get() = NetworkModule.getApiService()

    suspend fun getEvents(category: String? = null, search: String? = null): Result<List<EventOut>> =
        withContext(Dispatchers.IO) {
            try {
                val cat = if (category.equals("All", ignoreCase = true)) null else category
                val result = api.getEvents(cat, search)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getEventDetails(eventId: Int): Result<EventOut> =
        withContext(Dispatchers.IO) {
            try {
                val result = api.getEventDetails(eventId)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun registerForEvent(eventId: Int, payload: RegistrationIn): Result<RegistrationOut> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerForEvent(eventId, payload)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = NetworkModule.parseErrorDetail(response)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getRegistrations(eventId: Int? = null): Result<List<RegistrationItemOut>> =
        withContext(Dispatchers.IO) {
            try {
                val result = api.getRegistrations(eventId)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getStats(): Result<StatsOut> =
        withContext(Dispatchers.IO) {
            try {
                val result = api.getStats()
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createEvent(payload: EventCreate): Result<EventOut> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createEvent(payload)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = NetworkModule.parseErrorDetail(response)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun cancelRegistration(registrationId: Int): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.cancelRegistration(registrationId)
                if (response.isSuccessful) {
                    Result.success("Registration cancelled successfully")
                } else {
                    val errorMsg = NetworkModule.parseErrorDetail(response)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
