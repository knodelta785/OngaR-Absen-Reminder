package com.ongar.pengingatabsen

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ongar.pengingatabsen.databinding.ActivityAlarmSettingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Menu Alarm Absen Masuk / Alarm Absen Pulang (satu layar, dibedakan oleh EXTRA_TYPE). */
class AlarmSettingActivity : AppCompatActivity() {
    private lateinit var b: ActivityAlarmSettingBinding
    private lateinit var type: String
    private var updating = false

    private val nextFmt = SimpleDateFormat("EEEE, d MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getStringExtra(EXTRA_TYPE) ?: Store.TYPE_IN
        b = ActivityAlarmSettingBinding.inflate(layoutInflater)
        setContentView(b.root)

        val isIn = type == Store.TYPE_IN
        b.toolbar.title = if (isIn) "Alarm Absen Masuk" else "Alarm Absen Pulang"
        b.toolbar.setNavigationOnClickListener { finish() }
        b.tvInfo.text = if (isIn) {
            "Alarm berbunyi pada jam yang diatur, di hari pengingat yang dipilih. " +
                "Alarm otomatis OFF untuk hari itu begitu Anda menekan \"Sudah absen masuk\"."
        } else {
            "Alarm berbunyi pada jam yang diatur, di hari pengingat yang dipilih. " +
                "Alarm otomatis OFF untuk hari itu begitu Anda menekan \"Sudah absen pulang\"."
        }

        b.btnChangeTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, m ->
                    Store.setAlarmTime(this, type, h, m)
                    Scheduler.scheduleAll(this)
                    Notifier.sync(this)
                    refresh()
                },
                Store.alarmHour(this, type), Store.alarmMinute(this, type), true
            ).show()
        }

        b.switchAlarm.setOnCheckedChangeListener { _, checked ->
            if (updating) return@setOnCheckedChangeListener
            Store.setAlarmEnabled(this, type, checked)
            if (!checked && AlarmService.ringingType == type) AlarmService.stop(this)
            Scheduler.scheduleAll(this)
            refresh()
        }

        b.btnTest.setOnClickListener { AlarmService.start(this, type, test = true) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val enabled = Store.alarmEnabled(this, type)
        val done = Store.isDone(this, type) && Store.isTodayEnabled(this)

        b.tvTime.text = Store.scheduleLabel(this, type)

        updating = true
        b.switchAlarm.isChecked = enabled
        updating = false

        val (label, color) = when {
            !enabled -> "NONAKTIF" to R.color.text_secondary
            done -> "OFF OTOMATIS • SUDAH ABSEN" to R.color.green
            else -> "AKTIF" to R.color.blue
        }
        b.tvStatus.text = label
        b.tvStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))

        val next = Scheduler.nextAlarmMillis(this, type)
        b.tvNext.text = if (next != null) {
            "Berbunyi berikutnya: " + nextFmt.format(Date(next))
        } else {
            "Tidak ada jadwal alarm (alarm nonaktif atau tidak ada hari yang dipilih)."
        }
    }
}
