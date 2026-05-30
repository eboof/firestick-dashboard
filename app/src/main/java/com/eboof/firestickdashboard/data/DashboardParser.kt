package com.eboof.firestickdashboard.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object DashboardParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): DashboardState {
        val root = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrElse {
            return DashboardState(rawJson = raw)
        }

        val weatherNode = root.firstObject("weather")
        val headlinesNode = root.firstArray("news", "headlines")
        val stocksNode = root.firstArray("stocks", "watchlist", "market")
        val portfolioNode = root.firstArray("portfolio", "holdings")

        return DashboardState(
            weather = weatherNode?.toWeather(),
            headlines = headlinesNode?.toHeadlines().orEmpty(),
            stocks = stocksNode?.toStocks().orEmpty(),
            portfolio = portfolioNode?.toPortfolio().orEmpty(),
            lastUpdated = root.firstString("updated_at", "last_updated", "timestamp", "generated_at"),
            rawJson = raw
        )
    }

    private fun JsonObject.toWeather(): WeatherSummary = WeatherSummary(
        location = firstString("location", "name", "area"),
        temperature = firstAnyString("temperature", "temp_c", "temp", "feels_like"),
        condition = firstString("condition", "summary", "description", "text"),
        humidity = firstAnyString("humidity"),
        wind = firstAnyString("wind", "wind_kph", "wind_speed")
    )

    private fun JsonArray.toHeadlines(): List<String> = mapNotNull { item ->
        when (item) {
            is JsonPrimitive -> item.contentOrNull
            is JsonObject -> item.firstString("headline", "title", "name")
            else -> null
        }
    }

    private fun JsonArray.toStocks(): List<StockQuote> = mapNotNull { item ->
        val obj = item as? JsonObject ?: return@mapNotNull null
        val symbol = obj.firstString("symbol", "ticker", "name") ?: return@mapNotNull null
        StockQuote(
            symbol = symbol,
            price = obj.firstAnyString("price", "last", "value"),
            change = obj.firstAnyString("change", "change_percent", "percent_change")
        )
    }

    private fun JsonArray.toPortfolio(): List<PortfolioItem> = mapNotNull { item ->
        when (item) {
            is JsonObject -> {
                val name = item.firstString("name", "symbol", "asset") ?: return@mapNotNull null
                PortfolioItem(
                    name = name,
                    value = item.firstAnyString("value", "market_value", "price"),
                    change = item.firstAnyString("change", "pnl", "day_change")
                )
            }
            is JsonPrimitive -> item.contentOrNull?.let { PortfolioItem(name = it) }
            else -> null
        }
    }

    private fun JsonObject.firstString(vararg keys: String): String? =
        keys.asSequence().mapNotNull { key -> (this[key] as? JsonPrimitive)?.contentOrNull }.firstOrNull()

    private fun JsonObject.firstAnyString(vararg keys: String): String? =
        keys.asSequence().mapNotNull { key -> this[key].stringifyValue() }.firstOrNull()

    private fun JsonObject.firstObject(vararg keys: String): JsonObject? =
        keys.asSequence().mapNotNull { key -> this[key] as? JsonObject }.firstOrNull()

    private fun JsonObject.firstArray(vararg keys: String): JsonArray? =
        keys.asSequence().mapNotNull { key -> this[key] as? JsonArray }.firstOrNull()

    private fun JsonElement?.stringifyValue(): String? = when (this) {
        is JsonPrimitive -> contentOrNull ?: booleanOrNull?.toString()
        is JsonObject -> null
        is JsonArray -> null
        else -> null
    }
}
