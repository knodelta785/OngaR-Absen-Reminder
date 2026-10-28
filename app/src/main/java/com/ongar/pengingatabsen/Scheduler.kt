package com.ongar.pengingatabsen

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/** Penjadwalan alarm bunyi (07:30 / 16:30) dan pemicu notifikasi (00:01 / 16:31). */
object Scheduler {
    const val ACTION_ALARM = "com.ongar.pengingatabsen.ACTION_ALARM"
    const val ACTION_REMIND = "com.ongar.pengingatabsen.ACTION_REMIND"

    private const val CODE_ALARM = 10
    private const val CODE_SNOOZE = 20
    private const val CODE_REMIND = 30
    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    private fun idx(type: String) = if (type == Store.TYPE_IN) 0 else 1

    private fun pi(ctx: Context, action: String, type: String, code: Int): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).setAction(action).putExtra(EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(ctx, code, i, FLAGS)
    }

    /** Menit (dari 00:00) saat notifikasi "belum absen" mulai muncul. */
    fun reminderMinutes(ctx: Context, type: String): Int =
        if (type == Store.TYPE_IN) 1 else minOf(Store.alarmMinutes(ctx, type) + 1, 1439)

    fun scheduleAll(ctx: Context) {
        val app = ctx.applicationContext
        for (type in listOf(Store.TYPE_IN, Store.TYPE_OUT)) {
            val alarmPi = pi(app, ACTION_ALARM, type, CODE_ALARM + idx(type))
            val next = nextAlarmMillis(app, type)
            if (next != null) setRinging(app, alarmPi, next) else cancel(app, alarmPi)

            val mins = reminderMinutes(app, type)
            val at = nextTime(mins / 60, mins % 60).timeInMillis
            setExact(app, pi(app, ACTION_REMIND, type, CODE_REMIND + idx(type)), at)
        }
    }

    /**
     * Waktu alarm berikutnya. Melewati hari yang tidak dipilih dan hari yang
     * absennya sudah dilakukan (alarm otomatis off untuk hari itu).
     */
    fun nextAlarmMillis(ctx: Context, type: String): Long? {
        if (!Store.alarmEnabled(ctx, type)) return null
        val cal = nextTime(Store.alarmHour(ctx, type), Store.alarmMinute(ctx, type))
        repeat(8) {
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (Store.isDayEnabled(ctx, dow) && !Store.isDone(ctx, type, Store.dateKey(cal))) {
                return cal.timeInMillis
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return null
    }

    fun snooze(ctx: Context, type: String, minutes: Int) {
        val app = ctx.applicationContext
        val at = System.currentTimeMillis() + minutes * 60_000L
        setRinging(app, pi(app, ACTION_ALARM, type, CODE_SNOOZE + idx(type)), at)
    }

    fun cancelSnooze(ctx: Context, type: String) {
        val app = ctx.applicationContext
        cancel(app, pi(app, ACTION_ALARM, type, CODE_SNOOZE + idx(type)))
    }

    private fun nextTime(h: Int, m: Int): Calendar {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, h)
        c.set(Calendar.MINUTE, m)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (c.timeInMillis <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_MONTH, 1)
        return c
    }

    private fun setRinging(ctx: Context, pi: PendingIntent, at: Long) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val show = PendingIntent.getActivity(ctx, 99, Intent(ctx, MainActivity::class.java), FLAGS)
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), pi)
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun setExact(ctx: Context, pi: PendingIntent, at: Long) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun cancel(ctx: Context, pi: PendingIntent) {
        ctx.getSystemService(AlarmManager::class.java).cancel(pi)
        pi.cancel()
    }
}
