package com.poweralarm.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class AlarmSerializationTest {

    @Test
    fun `alarm round-trips through JSON`() {
        val alarm = Alarm(
            id = 42,
            label = "Wake up",
            hour = 6,
            minute = 30,
            recurrence = Recurrence.Weekly(setOf(1, 2, 3, 4, 5)),
            conditions = listOf(
                Condition.HolidaySkip(regionTag = "GB", provider = "nager"),
                Condition.WeatherAdvance("openweather", 10, 15, 1f, 1f),
            ),
            dismissalRequirements = listOf(
                DismissalRequirement.Cognitive(listOf("probability"), "hard", 2),
                DismissalRequirement.Voice("hash", 0.9f),
            ),
            audioPlan = AudioPlan(source = AudioSource.SpotifyPlaylist("spotify:playlist:abc")),
        )
        val encoded = DomainJson.encodeToString(Alarm.serializer(), alarm)
        val decoded = DomainJson.decodeFromString(Alarm.serializer(), encoded)
        assertThat(decoded).isEqualTo(alarm)
    }

    @Test
    fun `every recurrence variant serializes`() {
        val variants = listOf(
            Recurrence.Once,
            Recurrence.Daily,
            Recurrence.Weekly(setOf(0, 6)),
            Recurrence.ShiftPattern("[]", "2026-01-01"),
            Recurrence.Polyphasic("everyman", "23:00"),
            Recurrence.SolarAnchored("sunrise", -15),
            Recurrence.Adhan("fajr", "MWL", "shafi"),
            Recurrence.Chained(parentId = 1L, offsetMin = 5),
            Recurrence.Cron("0 7 * * 1-5"),
        )
        variants.forEach { v ->
            val encoded = DomainJson.encodeToString(Recurrence.serializer(), v)
            val decoded = DomainJson.decodeFromString(Recurrence.serializer(), encoded)
            assertThat(decoded).isEqualTo(v)
        }
    }

    @Test
    fun `every dismissal requirement serializes`() {
        val variants = listOf(
            DismissalRequirement.TapButton,
            DismissalRequirement.Cognitive(listOf("algebra"), "easy", 1),
            DismissalRequirement.Voice("h", 0.85f),
            DismissalRequirement.Nfc("uid"),
            DismissalRequirement.Qr("hash"),
            DismissalRequirement.Steps(30, 5),
            DismissalRequirement.EyesOpenSelfie(0.7f, 5),
            DismissalRequirement.MotionSustained(60, 1.5f),
            DismissalRequirement.Distress("h"),
        )
        variants.forEach { v ->
            val encoded = DomainJson.encodeToString(DismissalRequirement.serializer(), v)
            val decoded = DomainJson.decodeFromString(DismissalRequirement.serializer(), encoded)
            assertThat(decoded).isEqualTo(v)
        }
    }
}
