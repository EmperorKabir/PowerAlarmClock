package com.poweralarm.integrations.emergency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface UsgsApi {
    @GET("fdsnws/event/1/query")
    suspend fun events(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTimeIso: String,
        @Query("minmagnitude") minMagnitude: Float,
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("maxradiuskm") maxRadiusKm: Int,
    ): UsgsResponse
}

@Serializable
data class UsgsResponse(@SerialName("features") val features: List<UsgsFeature> = emptyList())

@Serializable
data class UsgsFeature(@SerialName("properties") val properties: UsgsProperties)

@Serializable
data class UsgsProperties(
    @SerialName("mag") val magnitude: Float,
    @SerialName("place") val place: String,
    @SerialName("time") val timeMs: Long,
)

class EarthquakeMonitor(private val api: UsgsApi) {

    suspend fun shouldFire(
        startTimeIso: String,
        minMagnitude: Float,
        lat: Float,
        lng: Float,
        radiusKm: Int,
    ): Boolean = api.events(
        startTimeIso = startTimeIso,
        minMagnitude = minMagnitude,
        latitude = lat,
        longitude = lng,
        maxRadiusKm = radiusKm,
    ).features.isNotEmpty()
}
