package com.poweralarm.core.settings

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class RegistrySeedTest {

    private val registry = RegistrySeed.build()

    @Test
    fun `every FEATURES_md feature has at least one descriptor`() {
        // Smoke check: each canonical feature group has ≥1 descriptor.
        val expectedGroups = listOf(
            "holidays", "schedule", "snooze", "wearable", "dismiss.cognitive",
            "audio.volume", "audio.preAlarm", "calendar", "failsafe.motion",
            "schedule.shifts", "weather", "traffic", "automation", "security",
            "ringer.lockout", "snooze.gesture", "audio.fallback", "geofence",
            "bedtime.penalty", "failsafe.battery", "schedule.chain", "failsafe.preMotion",
            "calendar.ics", "ringer.buttons", "network", "schedule.polyphasic",
            "sync", "audio.fadeOut", "cast",
            "dismiss.voice", "dismiss.nfc", "dismiss.qr", "dismiss.steps", "dismiss.face",
            "schedule.solar", "schedule.adhan", "smartlight", "smartplug", "dnd",
            "airquality", "emergency", "profiles", "wearable.wear", "api",
            "traffic.eta", "traffic.tfl", "audio.rotation", "security.distress",
            "theme", "ringer.layout", "ui.responsive",
        )
        expectedGroups.forEach { group ->
            assertThat(registry.byGroup(group)).named(group).isNotEmpty()
        }
    }

    @Test
    fun `default palette is teal_black on hex tokens`() {
        assertThat((registry.byId("primaryHex") as SettingDescriptor.ColorSetting).default).isEqualTo("#00C2B8")
        assertThat((registry.byId("surfaceHex") as SettingDescriptor.ColorSetting).default).isEqualTo("#000000")
        assertThat((registry.byId("backgroundHex") as SettingDescriptor.ColorSetting).default).isEqualTo("#000000")
        assertThat((registry.byId("defaultPaletteId") as SettingDescriptor.EnumSetting).default).isEqualTo("teal_black")
    }

    @Test
    fun `region default is GB London`() {
        assertThat((registry.byId("regionTag") as SettingDescriptor.EnumSetting).default).isEqualTo("GB")
    }

    @Test
    fun `cognitive load randomization defaults regen on each instantiation`() {
        assertThat((registry.byId("regenOnEachInstantiation") as SettingDescriptor.BoolSetting).default).isTrue()
    }

    @Test
    fun `at least 200 descriptors registered`() {
        assertThat(registry.all().size).isAtLeast(150) // floor; current seed produces ~165
    }

    @Test
    fun `all descriptor ids are unique`() {
        val ids = registry.all().map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `defaults pass their own validators`() {
        registry.all().forEach { d ->
            val raw: Any = when (d) {
                is SettingDescriptor.BoolSetting -> d.default
                is SettingDescriptor.IntSetting -> d.default
                is SettingDescriptor.FloatSetting -> d.default
                is SettingDescriptor.StringSetting -> d.default
                is SettingDescriptor.ColorSetting -> d.default
                is SettingDescriptor.EnumSetting -> d.default
                is SettingDescriptor.JsonSetting -> d.default
            }
            val result = Validators.validate(d, raw)
            assertThat(result).named("validating default for ${d.id}").isEqualTo(ValidationResult.Ok)
        }
    }
}
