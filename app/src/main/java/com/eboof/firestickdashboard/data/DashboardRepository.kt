package com.eboof.firestickdashboard.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class DashboardRepository(
    private val baseUrl: String,
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    fun fetchState(): DashboardState {
        val request = Request.Builder()
            .url(baseUrl + "api/state")
            .apply {
                if (apiKey.isNotBlank()) {
                    addHeader("X-API-Key", apiKey)
                }
            }
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            return DashboardParser.parse(body)
        }
    }
}
