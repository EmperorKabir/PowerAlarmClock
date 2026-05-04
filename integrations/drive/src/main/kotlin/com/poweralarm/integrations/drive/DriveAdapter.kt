package com.poweralarm.integrations.drive

import com.poweralarm.core.audio.AudioSourcePort
import com.poweralarm.core.audio.ResolvedSource
import com.poweralarm.core.domain.model.AudioSource
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Drive API uses the **drive.file** scope EXCLUSIVELY. Files are selected via Google
 * Picker (which grants per-file access without elevating scope). The adapter resolves
 * a fileId into a streaming alt=media URL with a fresh OAuth access token.
 */
interface DriveApi {
    @GET("drive/v3/files/{fileId}")
    suspend fun metadata(
        @Header("Authorization") bearer: String,
        @Path("fileId") fileId: String,
        @Query("fields") fields: String = "id,name,mimeType,size",
        @Query("supportsAllDrives") supportsAllDrives: Boolean = true,
    ): DriveFile
}

@kotlinx.serialization.Serializable
data class DriveFile(val id: String, val name: String, val mimeType: String? = null, val size: String? = null)

class DriveAdapter(
    private val api: DriveApi,
    private val tokenProvider: suspend () -> String,
) : AudioSourcePort {
    override val supports: Class<out AudioSource> = AudioSource.DriveFile::class.java
    override suspend fun resolve(source: AudioSource): ResolvedSource {
        val fileId = (source as AudioSource.DriveFile).fileId
        val token = tokenProvider()
        val meta = api.metadata("Bearer $token", fileId)
        val playbackUri = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        return ResolvedSource(playbackUri = playbackUri, mimeType = meta.mimeType, streaming = true)
    }
}
