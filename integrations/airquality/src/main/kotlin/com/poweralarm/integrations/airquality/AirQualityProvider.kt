package com.poweralarm.integrations.airquality

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

data class AqiSnapshot(val pm25: Float, val pm10: Float, val no2: Float, val source: String)

interface AirQualityProvider {
    suspend fun current(lat: Float, lng: Float): AqiSnapshot
}

@Serializable
data class OpenAqResponse(@SerialName("results") val results: List<OpenAqResult> = emptyList())

@Serializable
data class OpenAqResult(@SerialName("measurements") val measurements: List<OpenAqMeasurement> = emptyList())

@Serializable
data class OpenAqMeasurement(
    @SerialName("parameter") val parameter: String,
    @SerialName("value") val value: Float,
)

interface OpenAqApi {
    @GET("v2/latest")
    suspend fun latest(
        @Query("coordinates") coordinates: String,
        @Query("radius") radiusMeters: Int = 5_000,
        @Query("limit") limit: Int = 1,
    ): OpenAqResponse
}

class OpenAqProvider(private val api: OpenAqApi) : AirQualityProvider {
    override suspend fun current(lat: Float, lng: Float): AqiSnapshot {
        val raw = api.latest("$lat,$lng")
        val ms = raw.results.firstOrNull()?.measurements ?: emptyList()
        return AqiSnapshot(
            pm25 = ms.firstOrNull { it.parameter == "pm25" }?.value ?: 0f,
            pm10 = ms.firstOrNull { it.parameter == "pm10" }?.value ?: 0f,
            no2 = ms.firstOrNull { it.parameter == "no2" }?.value ?: 0f,
            source = "openaq",
        )
    }
}
