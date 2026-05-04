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

    fun observe(): Flow<ThemeState> {
        val flows = listOf(
            "primaryHex", "secondaryHex", "tertiaryHex", "surfaceHex", "backgroundHex",
            "errorHex", "onPrimaryHex", "onSurfaceHex", "typographyFamily",
        ).map { store.observe<String>(it) }

        @Suppress("MagicNumber") // index into the descriptor list
        return combine(
            combine(flows) { it.toList() },
            store.observe<Int>("cornerRadiusDp"),
            store.observe<Float>("densityScale"),
            store.observe<Int>("motionDurationMs"),
            store.observe<Boolean>("darkModeAuto"),
            store.observe<Boolean>("useDynamicColor"),
        ) { strings, radius, density, motion, darkAuto, dynamic ->
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
                cornerRadiusDp = radius,
                densityScale = density,
                motionDurationMs = motion,
                darkModeAuto = darkAuto,
                useDynamicColor = dynamic,
            )
        }.distinctUntilChanged()
    }

    suspend fun setHex(role: ThemeRole, hex: String) {
        store.set(role.descriptorId, hex)
    }
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
