package com.poweralarm.integrations.spotify

import com.poweralarm.core.audio.AudioSourcePort
import com.poweralarm.core.audio.ResolvedSource
import com.poweralarm.core.domain.model.AudioSource

/**
 * Resolves a Spotify URI into a playback intent. App Remote handles the actual
 * playback; ExoPlayer is unused for these sources. The "playback URI" returned here
 * is the Spotify deep-link, consumed by [SpotifyController] (lives in :app).
 */
class SpotifyTrackAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.SpotifyTrack::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val uri = (source as AudioSource.SpotifyTrack).uri
        return ResolvedSource(playbackUri = uri, mimeType = "spotify/track", streaming = true)
    }
}

class SpotifyPlaylistAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.SpotifyPlaylist::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val uri = (source as AudioSource.SpotifyPlaylist).uri
        return ResolvedSource(playbackUri = uri, mimeType = "spotify/playlist", streaming = true)
    }
}

class SpotifyPodcastAdapter : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.SpotifyPodcast::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val uri = (source as AudioSource.SpotifyPodcast).uri
        return ResolvedSource(playbackUri = uri, mimeType = "spotify/podcast", streaming = true)
    }
}
