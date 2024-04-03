package com.giraffe.mynotificationsender

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "MyFirebaseMessagingServ"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        Log.i(TAG, "sendRegistrationToServer: $token")
        if (FirebaseUtils.isLoggedIn()) {
            FirebaseUtils.currentUserDetails().update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: ${message.from}")
        playSound()
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            Log.d(TAG, "Message data payload: 1 => ${message.data["notificationId"]}")
            Log.d(TAG, "Message data payload: 2 => ${message.data["title"]}")
            Log.d(TAG, "Message data payload: 3 => ${message.data["body"]}")
            sendNotification(
                message.data["notificationId"] ?: "",
                message.data["title"] ?: "",
                message.data["body"] ?: ""
            )
        }

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            //sendNotification(it.body ?: "")
        }
    }

    private fun sendNotification(notificationId: String, title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("notificationId", notificationId)
        intent.putExtra("title", title)
        intent.putExtra("body", messageBody)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val id = System.currentTimeMillis() / 1000
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            id.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH,
            )
            notificationManager.createNotificationChannel(channel)
        }

        //val notificationId = 0
        //val id = System.currentTimeMillis() / 1000
        notificationManager.notify(id.toInt(), notificationBuilder.build())
    }


    private fun playSound() {


        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val originalRingVolume = am.getStreamVolume(AudioManager.STREAM_RING)
        Log.i(TAG, "playSound: originalRingVolume => $originalRingVolume")
        val maxRingVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING)
        Log.i(TAG, "playSound: maxRingVolume => $maxRingVolume")
        am.setStreamVolume(
            AudioManager.STREAM_RING,
            maxRingVolume,
            0
        )

        val originalSystemVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM)
        Log.i(TAG, "playSound: originalSystemVolume => $originalSystemVolume")
        val maxSystemVolume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
        Log.i(TAG, "playSound: maxSystemVolume => $maxSystemVolume")
        am.setStreamVolume(
            AudioManager.STREAM_SYSTEM,
            maxSystemVolume,
            0
        )


        val originalNotificationVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        Log.i(TAG, "playSound: originalNotificationVolume => $originalNotificationVolume")
        val maxNotificationVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        Log.i(TAG, "playSound: maxNotificationVolume => $maxNotificationVolume")
        am.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            maxNotificationVolume,
            0
        )


        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()




        runBlocking {
            launch {
                delay(2000)
                am.setStreamVolume(AudioManager.STREAM_RING, originalRingVolume, 0)
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, originalSystemVolume, 0)
                am.setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    originalNotificationVolume,
                    0
                )
            }
        }

    }

}