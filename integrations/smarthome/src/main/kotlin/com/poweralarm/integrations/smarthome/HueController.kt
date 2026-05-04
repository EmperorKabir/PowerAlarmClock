package com.poweralarm.integrations.smarthome

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

/** Philips Hue bridge CLIP v2 client. */
interface HueApi {
    @PUT("clip/v2/resource/light/{id}")
    suspend fun update(
        @Header("hue-application-key") appKey: String,
        @Path("id") id: String,
        @Body body: HueLightUpdate,
    )
}

@Serializable
data class HueLightUpdate(
    @SerialName("on") val on: HueOn,
    @SerialName("dimming") val dimming: HueDim? = null,
    @SerialName("color_temperature") val colorTemperature: HueCt? = null,
)

@Serializable data class HueOn(@SerialName("on") val on: Boolean)
@Serializable data class HueDim(@SerialName("brightness") val brightness: Int)
@Serializable data class HueCt(@SerialName("mirek") val mirek: Int)

class HueController(private val api: HueApi, private val appKey: String) : SmartLightController {

    override suspend fun setBrightness(targetId: String, brightness: Float, kelvin: Int) {
        val pct = (brightness.coerceIn(0f, 1f) * PERCENT).toInt()
        api.update(
            appKey = appKey,
            id = targetId,
            body = HueLightUpdate(
                on = HueOn(on = pct > 0),
                dimming = HueDim(brightness = pct),
                colorTemperature = HueCt(mirek = mirekFromKelvin(kelvin)),
            ),
        )
    }

    override suspend fun off(targetId: String) {
        api.update(appKey = appKey, id = targetId, body = HueLightUpdate(on = HueOn(false)))
    }

    private fun mirekFromKelvin(k: Int): Int = (1_000_000 / k.coerceAtLeast(MIN_KELVIN))
        .coerceIn(MIN_MIREK, MAX_MIREK)

    private companion object {
        const val PERCENT = 100
        const val MIN_KELVIN = 1_000
        const val MIN_MIREK = 153
        const val MAX_MIREK = 500
    }
}
