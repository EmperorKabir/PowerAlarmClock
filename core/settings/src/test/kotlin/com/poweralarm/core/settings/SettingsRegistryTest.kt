package com.poweralarm.core.settings

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SettingsRegistryTest {

    @Test
    fun `byId returns descriptor`() {
        val a = SettingDescriptor.BoolSetting(id = "a", groupPath = "g", label = "A", default = false)
        val r = InMemorySettingsRegistry(listOf(a))
        assertThat(r.byId("a")).isSameInstanceAs(a)
        assertThat(r.byId("missing")).isNull()
    }

    @Test
    fun `byGroup matches exact and nested`() {
        val parent = SettingDescriptor.BoolSetting(id = "p", groupPath = "audio", label = "P", default = false)
        val child = SettingDescriptor.IntSetting(
            id = "c", groupPath = "audio.volume", label = "C", default = 5, range = 0..10,
        )
        val other = SettingDescriptor.BoolSetting(id = "o", groupPath = "ui", label = "O", default = false)
        val r = InMemorySettingsRegistry(listOf(parent, child, other))

        assertThat(r.byGroup("audio")).containsExactly(parent, child)
        assertThat(r.byGroup("ui")).containsExactly(other)
        assertThat(r.byGroup("nope")).isEmpty()
    }

    @Test
    fun `duplicate ids fail fast`() {
        val a = SettingDescriptor.BoolSetting(id = "x", groupPath = "g", label = "A", default = false)
        val b = SettingDescriptor.BoolSetting(id = "x", groupPath = "g", label = "B", default = true)
        val ex = assertThrows<IllegalArgumentException> { InMemorySettingsRegistry(listOf(a, b)) }
        assertThat(ex.message).contains("x")
    }

    @Test
    fun `unknown dependency fails fast`() {
        val a = SettingDescriptor.BoolSetting(
            id = "a", groupPath = "g", label = "A", default = false, dependsOn = listOf("missing"),
        )
        val ex = assertThrows<IllegalArgumentException> { InMemorySettingsRegistry(listOf(a)) }
        assertThat(ex.message).contains("missing")
    }
}
