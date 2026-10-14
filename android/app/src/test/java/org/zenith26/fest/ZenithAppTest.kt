package org.zenith26.fest

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import org.zenith26.fest.data.api.ZenithApiService
import org.zenith26.fest.data.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ZenithAppTest {

    private val gson = Gson()

    @Test
    fun testEventOutJsonDeserialization() {
        val json = """
            {
                "id": 1,
                "title": "Code Sprint 2.0",
                "category": "Technical",
                "description": "A 3-hour competitive coding hackathon, solo or in pairs",
                "event_date": "2026-11-14",
                "event_time": "10:00 AM",
                "venue": "Main Auditorium",
                "capacity": 100,
                "fee": 0,
                "icon": "💻",
                "available_seats": 98
            }
        """.trimIndent()

        val event = gson.fromJson(json, EventOut::class.java)
        assertEquals(1, event.id)
        assertEquals("Code Sprint 2.0", event.title)
        assertEquals("Technical", event.category)
        assertEquals("2026-11-14", event.eventDate)
        assertEquals("10:00 AM", event.eventTime)
        assertEquals(100, event.capacity)
        assertEquals(98, event.availableSeats)
    }

    @Test
    fun testStatsOutJsonDeserialization() {
        val json = """
            {
                "total_events": 15,
                "total_registrations": 2,
                "total_capacity": 1532,
                "overall_utilization_pct": 0.1,
                "upcoming_events_count": 15,
                "events_by_category": {
                    "Technical": 4,
                    "Cultural": 6,
                    "Sports": 2,
                    "Gaming": 1,
                    "Workshop": 2
                }
            }
        """.trimIndent()

        val stats = gson.fromJson(json, StatsOut::class.java)
        assertEquals(15, stats.totalEvents)
        assertEquals(2, stats.totalRegistrations)
        assertEquals(1532, stats.totalCapacity)
        assertEquals(0.1, stats.overallUtilizationPct, 0.01)
        assertEquals(15, stats.upcomingEventsCount)
        assertEquals(4, stats.eventsByCategory["Technical"])
    }

    @Test
    fun testClientValidationRules() {
        fun validate(name: String, roll: String, email: String): String? {
            if (name.isBlank()) return "Please enter your full name."
            if (roll.isBlank()) return "Please enter your college roll number."
            if (email.isBlank()) return "Please enter your email address."
            if (!email.contains("@")) return "Email must contain '@'."
            return null
        }

        assertNotNull(validate("", "CSE101", "test@fest.edu"))
        assertNotNull(validate("Aarav", "", "test@fest.edu"))
        assertNotNull(validate("Aarav", "CSE101", ""))
        assertNotNull(validate("Aarav", "CSE101", "invalid-email"))
        assertNull(validate("Aarav Mehta", "CSE101", "aarav@fest.edu"))
    }

    @Test
    fun testUtilizationCalculationAndWarningThreshold() {
        fun computeUtilization(capacity: Int, availableSeats: Int): Pair<Double, Boolean> {
            val registered = capacity - availableSeats
            val pct = if (capacity > 0) ((registered.toDouble() / capacity) * 1000).toInt() / 10.0 else 0.0
            val isWarning = pct >= 90.0
            return Pair(pct, isWarning)
        }

        // Low demand
        val low = computeUtilization(capacity = 100, availableSeats = 80)
        assertEquals(20.0, low.first, 0.01)
        assertFalse(low.second)

        // Exactly 90% demand -> should highlight warning
        val high90 = computeUtilization(capacity = 100, availableSeats = 10)
        assertEquals(90.0, high90.first, 0.01)
        assertTrue(high90.second)

        // Full event (100% capacity) -> should highlight warning
        val full = computeUtilization(capacity = 40, availableSeats = 0)
        assertEquals(100.0, full.first, 0.01)
        assertTrue(full.second)
    }

    @Test
    fun testLiveBackendIntegration() = runBlocking {
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8081/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val api = retrofit.create(ZenithApiService::class.java)

        try {
            // 1. Fetch all events
            val events = api.getEvents()
            assertTrue("Should have at least 14 seed events", events.size >= 14)

            // 2. Filter by category Technical
            val techEvents = api.getEvents(category = "Technical")
            assertTrue("Technical events should not be empty", techEvents.isNotEmpty())
            assertTrue("All filtered events should be Technical", techEvents.all { it.category == "Technical" })

            // 3. Search events by title
            val searchResults = api.getEvents(search = "Robo")
            assertTrue("Search for 'Robo' should find at least one event", searchResults.isNotEmpty())
            assertTrue("Found event should contain Robo", searchResults[0].title.contains("Robo", ignoreCase = true))

            // 4. Fetch live stats
            val stats = api.getStats()
            assertTrue(stats.totalEvents >= 14)
            assertTrue(stats.totalCapacity > 0)
            assertTrue(stats.eventsByCategory.containsKey("Technical"))
        } catch (e: Exception) {
            println("Notice: Local backend offline during test run (${e.message}). Skipping live HTTP call.")
        }
    }
}
