package com.vueo.tv.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "TiveoTvUpdate"
private const val EXTRA_APK_NAME = "com.vueo.tv.update.APK_NAME"

internal fun submitTvUpdateSession(context: Context, apk: File) {
    val installer = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
        setAppPackageName(context.packageName)
        setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO)
        setSize(apk.length())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
        }
    }
    val sessionId = installer.createSession(params)
    try {
        val callback = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(context, TvUpdateInstallReceiver::class.java).putExtra(EXTRA_APK_NAME, apk.name),
            PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0),
        )
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("base.apk", 0, apk.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            session.commit(callback.intentSender)
        }
    } catch (failure: Exception) {
        runCatching { installer.abandonSession(sessionId) }
        throw failure
    }
}

internal fun launchTvUpdateInstaller(context: Context, apk: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.vueo.updates", apk)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        setClipData(ClipData.newRawUri("Tiveo TV update", uri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

class TvUpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.hasExtra(PackageInstaller.EXTRA_STATUS)) return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
        val apk = intent.getStringExtra(EXTRA_APK_NAME)
            ?.takeIf { it.matches(Regex("Tiveo-TV-update-[0-9]+-[0-9]+\\.apk")) }
            ?.let { File(File(context.cacheDir, "updates"), it) }

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val launched = runCatching {
                    val confirmation = checkNotNull(confirmationIntent(intent))
                    context.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure { Log.w(TAG, "Could not open update confirmation", it) }.isSuccess
                if (!launched) {
                    if (sessionId >= 0) {
                        runCatching { context.packageManager.packageInstaller.abandonSession(sessionId) }
                    }
                    fallback(context, apk)
                }
            }
            PackageInstaller.STATUS_SUCCESS, PackageInstaller.STATUS_FAILURE_ABORTED -> apk?.delete()
            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_INVALID -> fallback(context, apk)
            else -> {
                Log.w(TAG, "Update session $sessionId failed with status $status")
                apk?.delete()
                Toast.makeText(context, "Unable to install the Tiveo TV update.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fallback(context: Context, apk: File?) {
        runCatching {
            check(apk != null && apk.isFile)
            launchTvUpdateInstaller(context, apk)
        }.onFailure {
            Log.w(TAG, "Could not open the fallback installer", it)
            apk?.delete()
            Toast.makeText(context, "Unable to open the Android installer.", Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun confirmationIntent(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        }
}
