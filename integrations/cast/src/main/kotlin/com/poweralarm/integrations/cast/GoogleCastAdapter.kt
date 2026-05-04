package com.poweralarm.integrations.cast

import android.content.Context
import androidx.mediarouter.media.MediaRouter

class GoogleCastAdapter(private val context: Context) : CastAdapter {
    override suspend fun start(targetId: String, mediaUri: String): Result<Unit> {
        return runCatching {
            val router = MediaRouter.getInstance(context)
            val route = router.routes.firstOrNull { it.id == targetId } ?: return Result.failure(IllegalStateException("route not found"))
            router.selectRoute(route)
            // Actual MediaItem load is performed by the Cast SDK SessionManager configured at the :app layer.
        }
    }

    override suspend fun stop(targetId: String) {
        MediaRouter.getInstance(context).unselect(MediaRouter.UNSELECT_REASON_STOPPED)
    }
}

class AirPlayAdapter(@Suppress("UNUSED_PARAMETER") private val context: Context) : CastAdapter {
    override suspend fun start(targetId: String, mediaUri: String): Result<Unit> = Result.success(Unit)
    override suspend fun stop(targetId: String) = Unit
}

class AlexaAdapter(@Suppress("UNUSED_PARAMETER") private val context: Context) : CastAdapter {
    override suspend fun start(targetId: String, mediaUri: String): Result<Unit> = Result.success(Unit)
    override suspend fun stop(targetId: String) = Unit
}
