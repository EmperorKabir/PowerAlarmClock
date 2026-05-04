package com.poweralarm.core.audio

import com.poweralarm.core.domain.model.AudioSource

class LocalAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.Local::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val uri = (source as AudioSource.Local).uri
        return ResolvedSource(playbackUri = uri, mimeType = null, streaming = false)
    }
}

class UrlAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.Url::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val url = (source as AudioSource.Url).url
        return ResolvedSource(playbackUri = url, mimeType = null, streaming = true)
    }
}

class SystemDefaultAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.SystemDefault::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        return ResolvedSource(
            playbackUri = "content://settings/system/alarm_alert",
            mimeType = null,
            streaming = false,
        )
    }
}
