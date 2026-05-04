package com.poweralarm.integrations.cast

import android.content.Context
import android.net.wifi.WifiManager

interface CastAdapter {
    suspend fun start(targetId: String, mediaUri: String): Result<Unit>
    suspend fun stop(targetId: String)
}

/**
 * Acquires a `MulticastLock` for the duration of cast — the "transient one-shot LAN
 * permission grant" the spec calls for. Adapters do their own protocol work below.
 */
class MulticastLockHolder(private val context: Context) {
    private var lock: WifiManager.MulticastLock? = null

    fun acquire(tag: String) {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wm.createMulticastLock(tag).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    fun release() {
        lock?.takeIf { it.isHeld }?.release()
        lock = null
    }
}
