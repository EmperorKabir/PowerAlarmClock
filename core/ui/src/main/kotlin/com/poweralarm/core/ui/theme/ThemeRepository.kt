package com.poweralarm.core.ui.theme

import com.poweralarm.core.settings.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

data class ThemeState(
    val primaryHex: String,
    val secondaryHex: String,
    val tertiaryHex: String,
    val surfaceHex: String,
    val backgroundHex: String,
    val errorHex: String,
    val onPrimaryHex: String,
    val onSurfaceHex: String,
    val typographyFamily: String,
    val cornerRadiusDp: Int,
    val densityScale: Float,
    val motionDurationMs: Int,
    val darkModeAuto: Boolean,
    val useDynamicColor: Boolean,
)

class ThemeRepository(private val store: SettingsStore) {

    private fun stringFlow(id: String): Flow<String> = store.observe(id)
    private fun intFlow(id: String): Flow<Int> = store.observe(id)
    private fun floatFlow(id: String): Flow<Float> = store.observe(id)
    private fun boolFlow(id: String): Flow<Boolean> = store.observe(id)

    fun observe(): Flow<ThemeState> {
        val colourFlows: Flow<List<String>> = combine(
            stringFlow("primaryHex"),
            stringFlow("secondaryHex"),
            stringFlow("tertiaryHex"),
            stringFlow("surfaceHex"),
            stringFlow("backgroundHex"),
            stringFlow("errorHex"),
            stringFlow("onPrimaryHex"),
            stringFlow("onSurfaceHex"),
            stringFlow("typographyFamily"),
        ) { values: Array<String> -> values.toList() }

        val numericFlows: Flow<NumericTokens> = combine(
            intFlow("cornerRadiusDp"),
            floatFlow("densityScale"),
            intFlow("motionDurationMs"),
            boolFlow("darkModeAuto"),
            boolFlow("useDynamicColor"),
        ) { radius: Int, density: Float, motion: Int, darkAuto: Boolean, dynamic: Boolean ->
            NumericTokens(radius, density, motion, darkAuto, dynamic)
        }

        return combine(colourFlows, numericFlows) { strings, numeric ->
            ThemeState(
                primaryHex = strings[0],
                secondaryHex = strings[1],
                tertiaryHex = strings[2],
                surfaceHex = strings[3],
                backgroundHex = strings[4],
                errorHex = strings[5],
                onPrimaryHex = strings[6],
                onSurfaceHex = strings[7],
                typographyFamily = strings[8],
                cornerRadiusDp = numeric.cornerRadius,
                densityScale = numeric.density,
                motionDurationMs = numeric.motion,
                darkModeAuto = numeric.darkAuto,
                useDynamicColor = numeric.dynamic,
            )
        }.distinctUntilChanged()
    }

    suspend fun setHex(role: ThemeRole, hex: String) {
        store.set(role.descriptorId, hex)
    }

    private data class NumericTokens(
        val cornerRadius: Int,
        val density: Float,
        val motion: Int,
        val darkAuto: Boolean,
        val dynamic: Boolean,
    )
}

enum class ThemeRole(val descriptorId: String) {
    PRIMARY("primaryHex"),
    SECONDARY("secondaryHex"),
    TERTIARY("tertiaryHex"),
    SURFACE("surfaceHex"),
    BACKGROUND("backgroundHex"),
    ERROR("errorHex"),
    ON_PRIMARY("onPrimaryHex"),
    ON_SURFACE("onSurfaceHex"),
}
