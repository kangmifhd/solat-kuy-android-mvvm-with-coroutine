package com.programmergabut.solatkuy.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.util.EnumConfig
import com.programmergabut.solatkuy.util.EnumConfig.Companion.AWAIT_VIBRATE_MS
import com.programmergabut.solatkuy.util.EnumConfig.Companion.VIBRATE_MS


/*
 * Created by Katili Jiwo Adi Wiyono on 27/03/20.
 */

class NotificationHelper(context: Context): ContextWrapper(context) {

    private val channelNotificationPrayerID: String = "channel_notification_prayer_1"
    private val channelNotificationPrayerName: String = "channel_notification_prayer_name_1"
    private var mManager: NotificationManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelNotificationPrayer =  NotificationChannel(
                channelNotificationPrayerID,
                channelNotificationPrayerName,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channelNotificationPrayer.enableLights(true)
            channelNotificationPrayer.vibrationPattern = longArrayOf(0)
            channelNotificationPrayer.enableVibration(true)
            channelNotificationPrayer.lightColor = R.color.purple_500
            channelNotificationPrayer.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            getManager()?.createNotificationChannel(channelNotificationPrayer)
        }
    }

    fun getManager(): NotificationManager? {
        if(mManager == null)
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        return mManager
    }

    fun getPrayerReminderNC(pTime: String, pCity: String, pName: String, intent: PendingIntent): NotificationCompat.Builder {
        val message = if(pCity.isEmpty())
            "now is $pTime in - let's pray $pName"
        else
            "now is $pTime in $pCity let's pray $pName"

        val bitmap = BitmapFactory.decodeResource(
            resources, when (pName) {
                getString(R.string.fajr) -> R.drawable.img_fajr
                getString(R.string.dhuhr) -> R.drawable.img_dhuhr
                getString(R.string.asr) -> R.drawable.img_asr
                getString(R.string.maghrib) -> R.drawable.img_maghrib
                getString(R.string.isha) -> R.drawable.img_isha
                else -> R.drawable.img_fajr
            }
        )

        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(vibrator.hasVibrator()){
            val vibrationPattern = longArrayOf(0, VIBRATE_MS, AWAIT_VIBRATE_MS, VIBRATE_MS, AWAIT_VIBRATE_MS, VIBRATE_MS, AWAIT_VIBRATE_MS, VIBRATE_MS)
            val mAmplitudes = intArrayOf(0, 225, 0, 225, 0, 225, 0, 225)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, mAmplitudes, -1))
            else
                vibrator.vibrate(vibrationPattern, -1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return NotificationCompat.Builder(applicationContext, channelNotificationPrayerID)
                .setContentTitle(pName)
                .setContentText(message)
                .setSubText(EnumConfig.DUA_AFTER_ADHAN)
                .setColor(getColor(R.color.dark_500))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_notifications_active_24dp)
                .setContentIntent(intent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.DEFAULT_ALL)
        }
        else{
            return NotificationCompat.Builder(this, channelNotificationPrayerID)
                .setContentTitle(pName)
                .setContentText(message)
                .setSubText(EnumConfig.DUA_AFTER_ADHAN)
                .setVibrate(longArrayOf(500, 500, 500))
                .setSmallIcon(R.drawable.ic_notifications_active_24dp)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }
    }

}