package com.poweralarm.core.settings

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SettingDescriptorTest {

    @Test
    fun `BoolSetting accepts boolean and rejects others`() {
        val d = SettingDescriptor.BoolSetting(id = "x", groupPath = "g", label = "L", default = false)
        assertThat(Validators.validate(d, true)).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "true")).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `IntSetting enforces range`() {
        val d = SettingDescriptor.IntSetting(id = "x", groupPath = "g", label = "L", default = 5, range = 0..10)
        assertThat(Validators.validate(d, 5)).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, -1)).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat(Validators.validate(d, 11)).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `FloatSetting enforces inclusive range`() {
        val d = SettingDescriptor.FloatSetting(
            id = "x", groupPath = "g", label = "L", default = 0.5f, rangeStart = 0f, rangeEndInclusive = 1f,
        )
        assertThat(Validators.validate(d, 0f)).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, 1f)).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, 1.0001f)).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `ColorSetting requires valid hex`() {
        val d = SettingDescriptor.ColorSetting(id = "x", groupPath = "g", label = "L", default = "#000000")
        assertThat(Validators.validate(d, "#FFAA00")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "#FFAA0080")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "#FFF")).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat(Validators.validate(d, "rgb(0,0,0)")).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `EnumSetting limits to choices`() {
        val d = SettingDescriptor.EnumSetting(
            id = "x", groupPath = "g", label = "L", default = "a", choices = listOf("a", "b", "c"),
        )
        assertThat(Validators.validate(d, "b")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "z")).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `StringSetting respects optional regex pattern`() {
        val d = SettingDescriptor.StringSetting(
            id = "x", groupPath = "g", label = "L", default = "abc", pattern = "^[a-z]+$",
        )
        assertThat(Validators.validate(d, "hello")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "Hello")).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `JsonSetting accepts parseable shapes`() {
        val d = SettingDescriptor.JsonSetting(id = "x", groupPath = "g", label = "L", default = "{}")
        assertThat(Validators.validate(d, "{\"k\":1}")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "[1,2,3]")).isEqualTo(ValidationResult.Ok)
        assertThat(Validators.validate(d, "garbled")).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun `descriptors expose feature flag and dependsOn`() {
        val d = SettingDescriptor.BoolSetting(
            id = "child",
            groupPath = "g",
            label = "L",
            default = false,
            featureFlag = "feat.flag",
            dependsOn = listOf("parent"),
        )
        assertThat(d.featureFlag).isEqualTo("feat.flag")
        assertThat(d.dependsOn).containsExactly("parent")
    }
}
