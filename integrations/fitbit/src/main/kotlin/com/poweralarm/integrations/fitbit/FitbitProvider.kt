package com.poweralarm.integrations.fitbit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface SleepStageProvider {
    /** Returns the timestamp of the next light-sleep window inside [from..to], or null. */
    suspend fun nextLightSleepWindow(from: Instant, to: Instant): Instant?
}

interface FitbitApi {
    @GET("1.2/user/-/sleep/date/{date}.json")
    suspend fun sleep(
        @Header("Authorization") bearer: String,
        @Path("date") date: String,
    ): FitbitSleepResponse
}

@Serializable
data class FitbitSleepResponse(@SerialName("sleep") val sleep: List<FitbitSleepEntry> = emptyList())

@Serializable
data class FitbitSleepEntry(
    @SerialName("startTime") val startTime: String,
    @SerialName("levels") val levels: FitbitLevels = FitbitLevels(),
)

@Serializable
data class FitbitLevels(@SerialName("data") val data: List<FitbitLevelEntry> = emptyList())

@Serializable
data class FitbitLevelEntry(
    @SerialName("dateTime") val dateTime: String,
    @SerialName("level") val level: String,
    @SerialName("seconds") val seconds: Int,
)

class FitbitSleepProvider(
    private val api: FitbitApi,
    private val tokenProvider: suspend () -> String,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : SleepStageProvider {

    private val isoLocal = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override suspend fun nextLightSleepWindow(from: Instant, to: Instant): Instant? {
        val date = from.atZone(zoneId).toLocalDate().toString()
        val resp = api.sleep("Bearer ${tokenProvider()}", date)
        for (entry in resp.sleep) {
            for (level in entry.levels.data) {
                if (level.level != "light") continue
                val instant = LocalDateTime.parse(level.dateTime, isoLocal).atZone(zoneId).toInstant()
                if (!instant.isBefore(from) && !instant.isAfter(to)) return instant
            }
        }
        return null
    }
}
