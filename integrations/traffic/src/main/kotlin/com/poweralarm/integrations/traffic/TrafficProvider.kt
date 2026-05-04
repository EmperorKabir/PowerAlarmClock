package com.poweralarm.integrations.traffic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

data class TrafficEstimate(val durationSec: Int, val durationInTrafficSec: Int) {
    val delaySec: Int get() = (durationInTrafficSec - durationSec).coerceAtLeast(0)
    val delayMin: Int get() = delaySec / SECONDS_PER_MINUTE
    private companion object { const val SECONDS_PER_MINUTE = 60 }
}

interface TrafficProvider {
    suspend fun estimate(originLat: Float, originLng: Float, destLat: Float, destLng: Float): TrafficEstimate
}

@Serializable
data class GoogleDirectionsResponse(@SerialName("routes") val routes: List<GoogleRoute> = emptyList())

@Serializable
data class GoogleRoute(@SerialName("legs") val legs: List<GoogleLeg> = emptyList())

@Serializable
data class GoogleLeg(
    @SerialName("duration") val duration: GoogleValue,
    @SerialName("duration_in_traffic") val durationInTraffic: GoogleValue? = null,
)

@Serializable
data class GoogleValue(@SerialName("value") val value: Int)

interface GoogleDirectionsApi {
    @GET("directions/json")
    suspend fun directions(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("departure_time") departureTime: String = "now",
        @Query("traffic_model") trafficModel: String = "best_guess",
        @Query("key") apiKey: String,
    ): GoogleDirectionsResponse
}

class GoogleTrafficProvider(private val api: GoogleDirectionsApi, private val apiKey: String) : TrafficProvider {
    override suspend fun estimate(originLat: Float, originLng: Float, destLat: Float, destLng: Float): TrafficEstimate {
        val raw = api.directions(
            origin = "$originLat,$originLng",
            destination = "$destLat,$destLng",
            apiKey = apiKey,
        )
        val leg = raw.routes.firstOrNull()?.legs?.firstOrNull() ?: return TrafficEstimate(0, 0)
        return TrafficEstimate(
            durationSec = leg.duration.value,
            durationInTrafficSec = leg.durationInTraffic?.value ?: leg.duration.value,
        )
    }
}
