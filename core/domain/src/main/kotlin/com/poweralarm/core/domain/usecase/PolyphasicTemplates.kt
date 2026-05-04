package com.poweralarm.core.domain.usecase

import java.time.LocalTime

data class PolyphasicSlot(val label: String, val time: LocalTime)

object PolyphasicTemplates {

    fun expand(template: String, anchor: LocalTime): List<PolyphasicSlot> = when (template.lowercase()) {
        "monophasic" -> listOf(PolyphasicSlot("core", anchor))
        "biphasic" -> listOf(
            PolyphasicSlot("core", anchor),
            PolyphasicSlot("nap", anchor.plusHoursWrapped(SIESTA_HOURS)),
        )
        "everyman" -> everyman(anchor)
        "uberman" -> uberman(anchor)
        "dymaxion" -> dymaxion(anchor)
        else -> listOf(PolyphasicSlot("core", anchor))
    }

    private fun everyman(anchor: LocalTime): List<PolyphasicSlot> = listOf(
        PolyphasicSlot("core", anchor),
        PolyphasicSlot("nap1", anchor.plusHoursWrapped(EVERY_NAP1)),
        PolyphasicSlot("nap2", anchor.plusHoursWrapped(EVERY_NAP2)),
        PolyphasicSlot("nap3", anchor.plusHoursWrapped(EVERY_NAP3)),
    )

    private fun uberman(anchor: LocalTime): List<PolyphasicSlot> =
        (0 until UBERMAN_NAPS).map { i ->
            PolyphasicSlot("nap${i + 1}", anchor.plusHoursWrapped(i * UBERMAN_INTERVAL_HOURS))
        }

    private fun dymaxion(anchor: LocalTime): List<PolyphasicSlot> =
        (0 until DYMAXION_NAPS).map { i ->
            PolyphasicSlot("nap${i + 1}", anchor.plusHoursWrapped(i * DYMAXION_INTERVAL_HOURS))
        }

    private fun LocalTime.plusHoursWrapped(h: Int): LocalTime =
        plusHours((h % HOURS_PER_DAY).toLong())

    private const val SIESTA_HOURS = 14
    private const val EVERY_NAP1 = 6
    private const val EVERY_NAP2 = 12
    private const val EVERY_NAP3 = 18
    private const val UBERMAN_NAPS = 6
    private const val UBERMAN_INTERVAL_HOURS = 4
    private const val DYMAXION_NAPS = 4
    private const val DYMAXION_INTERVAL_HOURS = 6
    private const val HOURS_PER_DAY = 24
}
