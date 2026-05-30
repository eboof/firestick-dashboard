package com.eboof.firestickdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eboof.firestickdashboard.data.DashboardRepository
import com.eboof.firestickdashboard.theme.FirestickDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = DashboardRepository(
            baseUrls = listOf(
                BuildConfig.DASHBOARD_BASE_URL,
                BuildConfig.DASHBOARD_FALLBACK_BASE_URL
            ),
            apiKey = BuildConfig.DASHBOARD_API_KEY
        )

        setContent {
            FirestickDashboardTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModelFactory(repository))
                val uiState by vm.uiState.collectAsState()
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(
                        uiState = uiState,
                        onRefresh = vm::refresh
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: DashboardUiState,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(24.dp)
    ) {
        when {
            uiState.loading && uiState.state == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Header(
                            lastUpdated = uiState.state?.lastUpdated,
                            activeBaseUrl = uiState.activeBaseUrl,
                            error = uiState.error,
                            onRefresh = onRefresh
                        )
                    }

                    uiState.state?.weather?.let { weather ->
                        item { WeatherCard(weather) }
                    }

                    item {
                        HeadlinesCard(uiState.state?.headlines.orEmpty())
                    }

                    item {
                        StocksCard(uiState.state?.stocks.orEmpty())
                    }

                    item {
                        PortfolioCard(uiState.state?.portfolio.orEmpty())
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(lastUpdated: String?, activeBaseUrl: String?, error: String?, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ivy Dashboard", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                text = lastUpdated?.let { "Updated $it" } ?: "Waiting for data",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF9CA3AF)
            )
            if (activeBaseUrl != null) {
                Text(
                    text = activeBaseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
            if (error != null) {
                Text("Error: $error", color = Color(0xFFFCA5A5))
            }
        }
        Button(onClick = onRefresh, modifier = Modifier.focusable()) {
            Text("Refresh")
        }
    }
}

@Composable
private fun WeatherCard(weather: com.eboof.firestickdashboard.data.WeatherSummary) {
    SectionCard(title = "Weather") {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatBlock(weather.location ?: "Location")
            StatBlock(weather.temperature ?: "--")
            StatBlock(weather.condition ?: "--")
            StatBlock(weather.humidity?.let { "Humidity $it" } ?: "Humidity --")
            StatBlock(weather.wind?.let { "Wind $it" } ?: "Wind --")
        }
    }
}

@Composable
private fun HeadlinesCard(headlines: List<String>) {
    SectionCard(title = "Headlines") {
        if (headlines.isEmpty()) {
            EmptyText()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                headlines.take(8).forEachIndexed { index, headline ->
                    Text(
                        text = "${index + 1}. $headline",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StocksCard(stocks: List<com.eboof.firestickdashboard.data.StockQuote>) {
    SectionCard(title = "Watchlist") {
        if (stocks.isEmpty()) {
            EmptyText()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stocks.forEach { stock ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stock.symbol, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text(stock.price ?: "--", color = Color.White)
                            Text(stock.change ?: "--", color = tintForChange(stock.change))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioCard(items: List<com.eboof.firestickdashboard.data.PortfolioItem>) {
    SectionCard(title = "Portfolio") {
        if (items.isEmpty()) {
            EmptyText()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text(item.value ?: "--", color = Color.White)
                            Text(item.change ?: "--", color = tintForChange(item.change))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
            content()
        }
    }
}

@Composable
private fun StatBlock(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.width(140.dp)
    )
}

@Composable
private fun EmptyText() {
    Text("No data yet", color = Color(0xFF9CA3AF))
}

private fun tintForChange(change: String?): Color {
    val value = change.orEmpty()
    return when {
        value.startsWith("-") -> Color(0xFFFCA5A5)
        value.startsWith("+") -> Color(0xFF86EFAC)
        else -> Color.White
    }
}
