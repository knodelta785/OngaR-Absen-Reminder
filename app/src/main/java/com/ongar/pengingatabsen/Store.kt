package com.ongar.pengingatabsen

import android.content.Context
import java.util.Calendar
import java.util.Locale

const val EXTRA_TYPE = "type"
const val EXTRA_TEST = "test"

/** Penyimpanan lokal: data absen per tanggal, hari pengingat, dan pengaturan alarm. */
object Store {
    const val TYPE_IN = "in"
    const val TYPE_OUT = "out"

    private const val PREF = "pengingat_absen"
    private const val KEY_DAYS = "days_enabled"

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun dateKey(cal: Calendar = Calendar.getInstance()): String =
        String.format(
            Locale.US, "%04d-%02d-%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        )

    fun fmt(h: Int, m: Int): String = String.format(Locale.US, "%02d:%02d", h, m)

    // ---------- Data absen ----------
    fun getTime(ctx: Context, type: String, key: String = dateKey()): Long =
        prefs(ctx).getLong("${type}_$key", 0L)

    fun isDone(ctx: Context, type: String, key: String = dateKey()): Boolean =
        getTime(ctx, type, key) > 0L

    fun setTime(ctx: Context, type: String, millis: Long, key: String = dateKey()) {
        prefs(ctx).edit().putLong("${type}_$key", millis).apply()
    }

    fun resetToday(ctx: Context) {
        val k = dateKey()
        prefs(ctx).edit().remove("${TYPE_IN}_$k").remove("${TYPE_OUT}_$k").apply()
    }

    fun resetAll(ctx: Context) {
        val p = prefs(ctx)
        val e = p.edit()
        p.all.keys
            .filter { it.startsWith("${TYPE_IN}_") || it.startsWith("${TYPE_OUT}_") }
            .forEach { e.remove(it) }
        e.apply()
    }

    // ---------- Hari pengingat (default Senin-Jumat) ----------
    private val defaultDays = setOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    private fun days(ctx: Context): Set<Int> {
        val s = prefs(ctx).getStringSet(KEY_DAYS, null) ?: return defaultDays
        return s.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun isDayEnabled(ctx: Context, dow: Int): Boolean = dow in days(ctx)

    fun isTodayEnabled(ctx: Context): Boolean =
        isDayEnabled(ctx, Calendar.getInstance().get(Calendar.DAY_OF_WEEK))

    fun setDayEnabled(ctx: Context, dow: Int, enabled: Boolean) {
        val set = days(ctx).toMutableSet()
        if (enabled) set.add(dow) else set.remove(dow)
        prefs(ctx).edit().putStringSet(KEY_DAYS, set.map { it.toString() }.toSet()).apply()
    }

    // ---------- Pengaturan alarm ----------
    fun alarmEnabled(ctx: Context, type: String): Boolean =
        prefs(ctx).getBoolean("alarm_on_$type", true)

    fun setAlarmEnabled(ctx: Context, type: String, on: Boolean) {
        prefs(ctx).edit().putBoolean("alarm_on_$type", on).apply()
    }

    fun alarmHour(ctx: Context, type: String): Int =
        prefs(ctx).getInt("alarm_h_$type", if (type == TYPE_IN) 7 else 16)

    fun alarmMinute(ctx: Context, type: String): Int =
        prefs(ctx).getInt("alarm_m_$type", 30)

    fun setAlarmTime(ctx: Context, type: String, h: Int, m: Int) {
        prefs(ctx).edit().putInt("alarm_h_$type", h).putInt("alarm_m_$type", m).apply()
    }

    fun alarmMinutes(ctx: Context, type: String): Int =
        alarmHour(ctx, type) * 60 + alarmMinute(ctx, type)

    fun scheduleLabel(ctx: Context, type: String): String =
        fmt(alarmHour(ctx, type), alarmMinute(ctx, type))

    /** Alarm hanya berbunyi jika: aktif, hari ini hari pengingat, dan belum absen. */
    fun shouldRing(ctx: Context, type: String): Boolean =
        alarmEnabled(ctx, type) && isTodayEnabled(ctx) && !isDone(ctx, type)
}
