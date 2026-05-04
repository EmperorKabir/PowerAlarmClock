package com.poweralarm.integrations.holidays

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

interface HolidayProvider {
    suspend fun publicHolidays(year: Int, regionTag: String): List<LocalDate>
}

@Serializable
data class NagerHoliday(
    @SerialName("date") val date: String,
    @SerialName("name") val name: String,
    @SerialName("counties") val counties: List<String>? = null,
    @SerialName("types") val types: List<String> = emptyList(),
)

interface NagerApi {
    @GET("api/v3/PublicHolidays/{year}/{country}")
    suspend fun publicHolidays(
        @Path("year") year: Int,
        @Path("country") country: String,
    ): List<NagerHoliday>
}

class NagerHolidayProvider(private val api: NagerApi) : HolidayProvider {
    override suspend fun publicHolidays(year: Int, regionTag: String): List<LocalDate> =
        api.publicHolidays(year, regionTag).map { LocalDate.parse(it.date) }
}
