package com.poweralarm.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HexColorTest {

    @Test
    fun `parses 6-digit hex with full alpha`() {
        assertThat(HexColor.parse("#00C2B8")).isEqualTo(Color(0xFF00C2B8.toInt()))
    }

    @Test
    fun `parses 8-digit hex preserving alpha`() {
        assertThat(HexColor.parse("#80FF0000")).isEqualTo(Color(0x80FF0000.toInt()))
    }

    @Test
    fun `falls back on invalid input`() {
        assertThat(HexColor.parse("not a color", Color.White)).isEqualTo(Color.White)
        assertThat(HexColor.parse("#FFF", Color.White)).isEqualTo(Color.White)
    }

    @Test
    fun `validates hex pattern`() {
        assertThat(HexColor.isValid("#000000")).isTrue()
        assertThat(HexColor.isValid("#FFFFFFFF")).isTrue()
        assertThat(HexColor.isValid("#GGGGGG")).isFalse()
    }
}
