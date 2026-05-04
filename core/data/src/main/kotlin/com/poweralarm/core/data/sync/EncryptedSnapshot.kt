package com.poweralarm.core.data.sync

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/** AES-GCM client-side envelope for cloud-sync payloads. */
object EncryptedSnapshot {

    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val IV_LEN = 12
    private const val TAG_LEN_BITS = 128

    fun encrypt(payload: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LEN_BITS, iv))
        val ct = cipher.doFinal(payload)
        return iv + ct
    }

    fun decrypt(envelope: ByteArray, key: SecretKey): ByteArray {
        require(envelope.size > IV_LEN) { "envelope too short" }
        val iv = envelope.copyOfRange(0, IV_LEN)
        val ct = envelope.copyOfRange(IV_LEN, envelope.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LEN_BITS, iv))
        return cipher.doFinal(ct)
    }

    fun keyFromBytes(raw: ByteArray): SecretKey = SecretKeySpec(raw, "AES")
}
