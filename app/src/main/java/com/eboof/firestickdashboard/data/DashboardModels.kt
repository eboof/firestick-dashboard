package com.eboof.firestickdashboard.data

data class DashboardState(
    val weather: WeatherSummary? = null,
    val headlines: List<String> = emptyList(),
    val stocks: List<StockQuote> = emptyList(),
    val portfolio: List<PortfolioItem> = emptyList(),
    val lastUpdated: String? = null,
    val rawJson: String = ""
)

data class WeatherSummary(
    val location: String? = null,
    val temperature: String? = null,
    val condition: String? = null,
    val humidity: String? = null,
    val wind: String? = null
)

data class StockQuote(
    val symbol: String,
    val price: String? = null,
    val change: String? = null
)

data class PortfolioItem(
    val name: String,
    val value: String? = null,
    val change: String? = null
)
