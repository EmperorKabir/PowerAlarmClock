package com.poweralarm.integrations.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherSnapshot(val precipitationMm: Float, val minTempC: Float, val condition: String)

interface WeatherProvider {
    suspend fun forecast(lat: Float, lng: Float, hoursAhead: Int): WeatherSnapshot
}

@Serializable
data class OpenWeatherResponse(
    @SerialName("hourly") val hourly: List<OpenWeatherHour> = emptyList(),
)

@Serializable
data class OpenWeatherHour(
    @SerialName("temp") val tempK: Float,
    @SerialName("rain") val rain: OpenWeatherRain? = null,
    @SerialName("weather") val weather: List<OpenWeatherCondition> = emptyList(),
)

@Serializable
data class OpenWeatherRain(@SerialName("1h") val mm: Float = 0f)

@Serializable
data class OpenWeatherCondition(@SerialName("main") val main: String)

interface OpenWeatherApi {
    @GET("data/3.0/onecall")
    suspend fun oneCall(
        @Query("lat") lat: Float,
        @Query("lon") lng: Float,
        @Query("exclude") exclude: String = "current,minutely,daily,alerts",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String,
    ): OpenWeatherResponse
}

class OpenWeatherProvider(private val api: OpenWeatherApi, private val apiKey: String) : WeatherProvider {
    override suspend fun forecast(lat: Float, lng: Float, hoursAhead: Int): WeatherSnapshot {
        val raw = api.oneCall(lat = lat, lng = lng, units = "metric", apiKey = apiKey)
        val window = raw.hourly.take(hoursAhead.coerceAtLeast(1))
        val precipMm = window.maxOfOrNull { it.rain?.mm ?: 0f } ?: 0f
        val minTemp = window.minOfOrNull { it.tempK } ?: 0f
        val cond = window.firstOrNull()?.weather?.firstOrNull()?.main ?: "Clear"
        return WeatherSnapshot(precipMm, minTemp, cond)
    }
}
