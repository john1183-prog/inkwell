package com.john.inkwell.data.drive

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.john.inkwell.data.Block
import com.john.inkwell.data.InkwellRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"
private const val BACKUP_FILE_NAME = "inkwell_backup.json"
private const val DRIVE_FILES_URL = "https://www.googleapis.com/drive/v3/files"
private const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"

sealed class SyncResult {
    data class Success(val mergedCount: Int) : SyncResult()
    data class Failure(val message: String) : SyncResult()
}

/**
 * Talks to Google Drive's hidden per-app "appDataFolder" — a space only
 * this app can see or write to (not visible in the user's normal Drive
 * UI, and it doesn't count against their visible storage the way a
 * regular file would in most clients' eyes). One JSON file in there
 * holds every block; see [BackupCodec] for the merge strategy.
 *
 * Requires Google Cloud Console setup the user has to do once — see
 * README "Setting up Google Drive sync".
 */
class DriveSyncManager(private val context: Context, private val repository: InkwellRepository) {

    private val httpClient = OkHttpClient()

    private val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.appdata"))
        .build()

    val signInClient: GoogleSignInClient = GoogleSignIn.getClient(context, signInOptions)

    fun signInIntent(): Intent = signInClient.signInIntent

    fun lastSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching { com.google.android.gms.tasks.Tasks.await(signInClient.signOut()) }
    }

    /** Full two-way sync: pull remote, merge into Room, push merged set back. */
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        val account = lastSignedInAccount()
            ?: return@withContext SyncResult.Failure("Not signed in to Google Drive")

        try {
            val token = fetchAccessToken(account)
            val fileId = findBackupFileId(token)
            val remoteBlocks: List<Block> = if (fileId != null) {
                val json = downloadFile(token, fileId)
                BackupCodec.decode(json)
            } else {
                emptyList()
            }

            repository.mergeRemote(remoteBlocks)

            val merged = repository.allBlocksOnce()
            val payload = BackupCodec.encode(merged)
            if (fileId != null) {
                updateFile(token, fileId, payload)
            } else {
                createFile(token, payload)
            }

            SyncResult.Success(merged.size)
        } catch (e: IOException) {
            SyncResult.Failure(e.message ?: "Network error during sync")
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: "Sync failed")
        }
    }

    private fun fetchAccessToken(account: GoogleSignInAccount): String {
        val androidAccount: Account = account.account
            ?: throw IOException("Signed-in Google account is missing an Android Account handle")
        return GoogleAuthUtil.getToken(context, androidAccount, DRIVE_SCOPE)
    }

    private fun findBackupFileId(token: String): String? {
        val url = "$DRIVE_FILES_URL?spaces=appDataFolder&q=name='$BACKUP_FILE_NAME'&fields=files(id)"
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive list failed: ${response.code}")
            val body = response.body?.string().orEmpty()
            val files = JSONObject(body).optJSONArray("files") ?: return null
            return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
        }
    }

    private fun downloadFile(token: String, fileId: String): String {
        val url = "$DRIVE_FILES_URL/$fileId?alt=media"
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive download failed: ${response.code}")
            return response.body?.string().orEmpty()
        }
    }

    private fun createFile(token: String, jsonPayload: String) {
        val metadata = JSONObject().apply {
            put("name", BACKUP_FILE_NAME)
            put("parents", org.json.JSONArray().put("appDataFolder"))
        }
        multipartUpload(token, "$DRIVE_UPLOAD_URL?uploadType=multipart", metadata, jsonPayload)
    }

    private fun updateFile(token: String, fileId: String, jsonPayload: String) {
        val metadata = JSONObject().apply { put("name", BACKUP_FILE_NAME) }
        multipartUpload(
            token,
            "$DRIVE_UPLOAD_URL/$fileId?uploadType=multipart",
            metadata,
            jsonPayload,
            method = "PATCH"
        )
    }

    private fun multipartUpload(
        token: String,
        url: String,
        metadata: JSONObject,
        jsonPayload: String,
        method: String = "POST"
    ) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(
                MultipartBody.Part.create(
                    okhttp3.Headers.headersOf("Content-Type", "application/json; charset=UTF-8"),
                    metadata.toString().toRequestBody()
                )
            )
            .addPart(
                MultipartBody.Part.create(
                    okhttp3.Headers.headersOf("Content-Type", "application/json; charset=UTF-8"),
                    jsonPayload.toRequestBody("application/json".toMediaType())
                )
            )
            .build()

        val requestBuilder = Request.Builder().url(url).header("Authorization", "Bearer $token")
        val request = if (method == "PATCH") {
            requestBuilder.patch(body).build()
        } else {
            requestBuilder.post(body).build()
        }

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Drive upload failed: ${response.code} ${response.body?.string()}")
            }
        }
    }
}
