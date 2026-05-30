package com.eboof.firestickdashboard.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class DashboardRepository(
    private val baseUrls: List<String>,
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    fun fetchState(): Pair<String, DashboardState> {
        var lastError: IOException? = null

        for (baseUrl in baseUrls.distinct()) {
            val request = Request.Builder()
                .url(baseUrl + "api/state")
                .apply {
                    if (apiKey.isNotBlank()) {
                        addHeader("X-API-Key", apiKey)
                    }
                }
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code}")
                    }
                    val body = response.body?.string().orEmpty()
                    return baseUrl to DashboardParser.parse(body)
                }
            } catch (error: IOException) {
                lastError = IOException("$baseUrl: ${error.message}", error)
            }
        }

        throw lastError ?: IOException("No dashboard base URLs configured")
    }
}
