package com.poweralarm.core.audio

import com.poweralarm.core.domain.model.AudioSource

interface AudioSourcePort {
    val supports: Class<out AudioSource>
    suspend fun resolve(source: AudioSource): ResolvedSource
}

data class ResolvedSource(
    val playbackUri: String,
    val mimeType: String?,
    val streaming: Boolean,
)

class AudioSourceRegistry(private val ports: List<AudioSourcePort>) {
    fun portFor(source: AudioSource): AudioSourcePort =
        ports.firstOrNull { it.supports.isInstance(source) }
            ?: throw IllegalStateException("No AudioSourcePort for $source")
}
