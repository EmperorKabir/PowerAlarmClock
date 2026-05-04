package com.poweralarm.core.data.sync

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import javax.crypto.KeyGenerator
import org.junit.jupiter.api.assertThrows

class EncryptedSnapshotTest {

    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun `round-trips arbitrary payload`() {
        val payload = "alarms config v1 ☀️".encodeToByteArray()
        val encrypted = EncryptedSnapshot.encrypt(payload, key)
        val decrypted = EncryptedSnapshot.decrypt(encrypted, key)
        assertThat(decrypted).isEqualTo(payload)
    }

    @Test
    fun `tamper detection fails decryption`() {
        val payload = "secret".encodeToByteArray()
        val envelope = EncryptedSnapshot.encrypt(payload, key)
        envelope[envelope.size - 1] = (envelope.last().toInt() xor 0xFF).toByte()
        assertThrows<javax.crypto.AEADBadTagException> { EncryptedSnapshot.decrypt(envelope, key) }
    }
}
