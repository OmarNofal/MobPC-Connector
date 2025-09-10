package com.omar.pcconnector.notifications

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.omar.pcconnector.data.DevicesRepository
import com.omar.pcconnector.getRetrofit
import com.omar.pcconnector.network.connection.Connectivity
import com.omar.pcconnector.pcApi
import com.omar.pcconnector.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class NotificationListener : NotificationListenerService() {

    @Inject
    lateinit var devicesRepository: DevicesRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        // Don't notify for groups this will fix some double notifications
        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return
        if (sbn.isOngoing) return

        scope.launch { handleNewNotification(sbn) }
    }

    private suspend fun handleNewNotification(sbn: StatusBarNotification) {
        val notificationContent = sbn.extractNotificationContent() ?: return
        Log.d("Notification", notificationContent.toString())
        sendNotificationToPCs(notificationContent)
    }

    private suspend fun sendNotificationToPCs(notificationContent: NotificationContent) {

        val pairedDevices = devicesRepository.getAllPairedDevices()

        pairedDevices.forEach {


            if (!shouldSendNotification(it.deviceInfo.id, notificationContent.packageName)) return@forEach

            val device =
                Connectivity.findDevice(it.deviceInfo.id) ?: return@forEach

            try {
                val api = getRetrofit(
                    device.ip,
                    device.port,
                    it.token,
                    it.certificate
                ).pcApi()

                Log.d("Notification", "Sending to $device")

                with(notificationContent) {
                    api.sendNotification(
                        title, text, icon.toBase64(), appName
                    ).execute()
                }
            } catch (e: Exception) {
                Log.e(
                    "Notification",
                    "Failed to send notification to server: " + e.stackTraceToString()
                )
            }

        }
    }

    private fun shouldSendNotification(
        serverId: String,
        packageName: String
    ): Boolean {
        val serverPreferences =
            userPreferencesRepository.getServerPreferences(serverId)

        return serverPreferences.sendPhoneNotificationsToServer && !serverPreferences.notificationsExcludedPackagesList.contains(
            packageName
        )
    }

    private fun Icon?.toBase64(): String? {
        if (this == null) return null

        val iconBitmap =
            loadDrawable(this@NotificationListener)?.toBitmap() ?: return null

        val byteArrayOs = ByteArrayOutputStream()
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOs)

        val byteArray = byteArrayOs.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun StatusBarNotification.extractNotificationContent(): NotificationContent? {

        return try {
            val title =
                notification.extras.getCharSequence("android.title").toString()
            val text =
                notification.extras.getCharSequence("android.text").toString()
            val icon = notification.getLargeIcon() ?: notification.smallIcon
            val appName = getAppNameFromPackage(this.packageName)

            NotificationContent(title, text, icon, appName, this.packageName)
        } catch (e: Exception) {
            Log.e("Notification", e.stackTraceToString())
            null
        }

    }

    private fun getAppNameFromPackage(packageName: String): String {

        val pm = this.packageManager

        val appInfo = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: Exception) {
            return "<unknown>"
        }

        return pm.getApplicationLabel(appInfo).toString()
    }

    data class NotificationContent(
        val title: String,
        val text: String,
        val icon: Icon?,
        val appName: String,
        val packageName: String
    )

}