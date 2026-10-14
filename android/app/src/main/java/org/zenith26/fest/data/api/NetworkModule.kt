package org.zenith26.fest.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.zenith26.fest.ZenithApplication
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var currentApi: ZenithApiService? = null
    private var cachedBaseUrl: String? = null

    fun getApiService(): ZenithApiService {
        val baseUrl = ZenithApplication.instance.getBaseUrl()
        if (currentApi != null && cachedBaseUrl == baseUrl) {
            return currentApi!!
        }

        synchronized(this) {
            if (currentApi != null && cachedBaseUrl == baseUrl) {
                return currentApi!!
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            cachedBaseUrl = baseUrl
            val api = retrofit.create(ZenithApiService::class.java)
            currentApi = api
            return api
        }
    }

    fun parseErrorDetail(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: return "HTTP Error ${response.code()}"
            val json = gson.fromJson(errorBody, JsonObject::class.java)
            if (json.has("detail")) {
                val elem = json.get("detail")
                if (elem.isJsonPrimitive) {
                    elem.asString
                } else if (elem.isJsonArray) {
                    val arr = elem.asJsonArray
                    arr.joinToString("; ") { it.asJsonObject.get("msg")?.asString ?: it.toString() }
                } else {
                    elem.toString()
                }
            } else {
                errorBody
            }
        } catch (e: Exception) {
            "Error ${response.code()}: ${response.message()}"
        }
    }
}
