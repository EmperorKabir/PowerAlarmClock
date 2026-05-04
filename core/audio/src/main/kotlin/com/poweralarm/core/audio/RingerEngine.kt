package com.poweralarm.core.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.poweralarm.core.domain.model.AudioPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RingerEngine(
    private val context: Context,
    private val sourceRegistry: AudioSourceRegistry,
    private val scope: CoroutineScope,
) {
    private var player: ExoPlayer? = null
    private var rampJob: Job? = null
    private var fadeJob: Job? = null
    private var startedAt: Long = 0L

    suspend fun start(plan: AudioPlan) {
        stop(immediate = true)
        startedAt = System.currentTimeMillis()
        val resolved = try {
            sourceRegistry.portFor(plan.source).resolve(plan.source)
        } catch (t: Throwable) {
            ResolvedSource(playbackUri = plan.localFallbackUri, mimeType = null, streaming = false)
        }
        val p = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(resolved.playbackUri)))
            volume = plan.volumeStartPct / PERCENT
            prepare()
            playWhenReady = true
        }
        player = p
        rampJob = scope.launch { rampVolume(plan) }
    }

    suspend fun dismiss(plan: AudioPlan) {
        rampJob?.cancel()
        fadeJob?.cancel()
        fadeJob = scope.launch { fadeOut(plan) }
    }

    fun stop(immediate: Boolean) {
        rampJob?.cancel()
        if (immediate) fadeJob?.cancel()
        player?.release()
        player = null
    }

    private suspend fun rampVolume(plan: AudioPlan) {
        val durationMs = plan.volumeRampMin * MIN_TO_MS
        if (durationMs <= 0L) return
        val tickMs = TICK_MS
        var elapsed = 0L
        while (elapsed < durationMs) {
            val v = VolumeCurve.ramp(
                startPct = plan.volumeStartPct,
                endPct = plan.volumeEndPct,
                durationMs = durationMs,
                elapsedMs = elapsed,
                curve = plan.volumeCurve,
            )
            player?.volume = v
            delay(tickMs)
            elapsed += tickMs
        }
        player?.volume = plan.volumeEndPct / PERCENT
    }

    private suspend fun fadeOut(plan: AudioPlan) {
        val durationMs = plan.fadeOutSec * SEC_TO_MS.toLong()
        if (durationMs <= 0L) {
            stop(immediate = true)
            return
        }
        val startVolume = player?.volume ?: 1f
        var elapsed = 0L
        while (elapsed < durationMs) {
            val v = VolumeCurve.fadeOut(startVolume, elapsed, durationMs, plan.fadeOutCurve)
            player?.volume = v
            delay(TICK_MS)
            elapsed += TICK_MS
        }
        stop(immediate = true)
    }

    companion object {
        private const val MIN_TO_MS = 60_000L
        private const val SEC_TO_MS = 1_000
        private const val TICK_MS = 100L
        private const val PERCENT = 100f
    }
}
