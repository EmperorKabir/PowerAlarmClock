package com.poweralarm.integrations.tfl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

data class TflStatus(val lineId: String, val severity: Int, val description: String) {
    val isDisrupted: Boolean get() = severity in 1..DISRUPTION_CEILING
    private companion object { const val DISRUPTION_CEILING = 9 }
}

interface TflProvider {
    suspend fun status(lineIds: List<String>): List<TflStatus>
}

@Serializable
data class TflLineStatusEntry(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("lineStatuses") val lineStatuses: List<TflLineStatus> = emptyList(),
)

@Serializable
data class TflLineStatus(
    @SerialName("statusSeverity") val statusSeverity: Int,
    @SerialName("statusSeverityDescription") val statusSeverityDescription: String,
)

interface TflApi {
    @GET("Line/{lines}/Status")
    suspend fun status(@Path("lines") lines: String): List<TflLineStatusEntry>
}

class TflApiProvider(private val api: TflApi) : TflProvider {
    override suspend fun status(lineIds: List<String>): List<TflStatus> {
        if (lineIds.isEmpty()) return emptyList()
        val raw = api.status(lineIds.joinToString(","))
        return raw.flatMap { entry ->
            entry.lineStatuses.map {
                TflStatus(entry.id, it.statusSeverity, it.statusSeverityDescription)
            }
        }
    }
}
