package org.zenith26.fest

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class ZenithApplication : Application() {

    lateinit var prefs: SharedPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = getSharedPreferences("zenith_prefs", Context.MODE_PRIVATE)
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(url: String) {
        val sanitized = if (!url.endsWith("/")) "$url/" else url
        prefs.edit().putString(KEY_BASE_URL, sanitized).apply()
    }

    companion object {
        lateinit var instance: ZenithApplication
            private set

        const val DEFAULT_BASE_URL = "https://zenith26-fest.vercel.app/"
        private const val KEY_BASE_URL = "key_base_url"
    }
}
