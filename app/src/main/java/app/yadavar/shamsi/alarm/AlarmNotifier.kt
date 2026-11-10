package app.yadavar.shamsi.alarm

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.yadavar.shamsi.CHANNEL_ALARM
import app.yadavar.shamsi.CHANNEL_ID
import app.yadavar.shamsi.R
import app.yadavar.shamsi.model.Reminder
import app.yadavar.shamsi.model.formatTime
import app.yadavar.shamsi.model.rowMeta

object AlarmNotifier {
    fun placeholder(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("یادآور")
            .setContentText("وقت یادآور است")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()

    fun build(context: Context, reminder: Reminder, channelId: String, ongoing: Boolean): Notification {
        val id = reminder.id
        val open = PendingIntent.getActivity(
            context,
            id.hashCode(),
            Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("id", id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snooze10 = broadcast(context, id, AlarmScheduler.ACTION_SNOOZE, "snooze10", 10)
        val snooze15 = broadcast(context, id, AlarmScheduler.ACTION_SNOOZE, "snooze15", 15)
        val snooze30 = broadcast(context, id, AlarmScheduler.ACTION_SNOOZE, "snooze30", 30)
        val snooze60 = broadcast(context, id, AlarmScheduler.ACTION_SNOOZE, "snooze60", 60)
        val snoozeOther = PendingIntent.getActivity(
            context, "snoozeOther:$id".hashCode(),
            Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("id", id)
                putExtra(AlarmActivity.EXTRA_SNOOZE, true)
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val done = broadcast(context, id, AlarmScheduler.ACTION_DONE, "done")
        val close = broadcast(context, id, AlarmScheduler.ACTION_DISMISS, "dismiss")
        val time = if (reminder.hour != null && reminder.minute != null) formatTime(reminder.hour, reminder.minute) else ""
        val text = listOf(time, rowMeta(reminder, null, null)).filter { it.isNotBlank() }.joinToString(" · ")
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(reminder.title.ifBlank { "یادآور" })
            .setContentText(text.ifBlank { "وقت یادآور است" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(ongoing)
            .setAutoCancel(false)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .addAction(0, "انجام شد", done)
            .addAction(0, "۱۰ دقیقه", snooze10)
            .addAction(0, "۱۵ دقیقه", snooze15)
            .addAction(0, "۳۰ دقیقه", snooze30)
            .addAction(0, "۱ ساعت", snooze60)
            .addAction(0, "زمان دیگر", snoozeOther)
            .addAction(0, "بستن", close)
            .build()
    }

    /** Used only when the alarm service cannot start: the channel's own sound plays instead. */
    fun postFallback(context: Context, reminder: Reminder) {
        val notification = build(context, reminder, CHANNEL_ID, ongoing = false)
        NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
        val vibrator = context.getSystemService(Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    private fun broadcast(context: Context, id: String, action: String, salt: String, minutes: Int = 5): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("id", id)
            if (action == AlarmScheduler.ACTION_SNOOZE) putExtra("minutes", minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            "$salt:$id".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
