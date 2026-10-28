# PENGINGAT ABSEN (OngaR)

Aplikasi Android pengingat absen masuk dan absen pulang.

## Fitur

- **Absen masuk 07:30** dan **absen pulang 16:30** (jam alarm bisa diubah di menu Alarm).
- Default pengingat **Senin–Jumat**. Sabtu dan Minggu bisa diaktifkan lewat kartu *Hari Pengingat*.
  Hari libur dan tanggal merah tetap diingatkan sesuai hari yang dipilih.
- **Notifikasi status bar**
  - *Belum absen masuk* muncul sejak **00:01** sampai absen masuk dilakukan.
  - *Belum absen pulang* muncul sejak **16:31** sampai absen pulang dilakukan.
  - Hilang lewat tombol **Sudah absen** di aplikasi, atau lewat tombol kecil di notifikasi
    (seperti "Tandai dibaca" di WhatsApp).
- **Menu Alarm Absen Masuk** dan **Alarm Absen Pulang** (aktif/nonaktif, ubah jam, uji bunyi).
  Alarm otomatis OFF untuk hari itu begitu absen dilakukan, dan aktif lagi esok harinya.
- **Timestamp** jam:menit:detik saat absen dicatat, plus tombol *Ubah jam* bila perlu dikoreksi.
- **Reset data absen** (hari ini saja atau semua data) lewat menu titik tiga.
- Warna kartu **merah** = belum absen, **hijau** = sudah absen.
- Riwayat 7 hari terakhir.

## Cara mendapatkan APK lewat GitHub (tanpa Android Studio)

1. Buat repository baru di GitHub, lalu upload seluruh isi folder proyek ini
   (pastikan folder `.github` ikut terupload).
2. Buka tab **Actions** → workflow **Build APK** → tunggu sampai selesai (sekitar 3–5 menit).
   Bisa juga dijalankan manual dengan tombol *Run workflow*.
3. Buka hasil run tersebut, unduh artifact **PENGINGAT-ABSEN-apk**, ekstrak, lalu pasang
   `app-debug.apk` di HP (izinkan "Install dari sumber tidak dikenal").

## Cara lain: Android Studio

Buka folder ini di Android Studio (Hedgehog atau lebih baru), tunggu Gradle sync, lalu
**Build → Build APK(s)**.

## Pengaturan penting di HP

Buka menu titik tiga → **Izin & Baterai**, lalu:

- Izinkan **notifikasi**.
- Pilih **tanpa pembatasan** pada optimasi baterai agar alarm tepat waktu.
- Android 14: izinkan **notifikasi layar penuh** agar layar alarm muncul di atas kunci layar.
- Beberapa merek HP (Xiaomi, Oppo, Vivo, Realme, Samsung) perlu mengaktifkan **Mulai otomatis / Autostart**
  untuk aplikasi ini di pengaturan sistem.

## Struktur singkat

| File | Fungsi |
|------|--------|
| `Store.kt` | Penyimpanan data absen, hari pengingat, pengaturan alarm |
| `Scheduler.kt` | Penjadwalan alarm 07:30 / 16:30 dan pemicu notifikasi 00:01 / 16:31 |
| `Notifier.kt` | Notifikasi status bar dan notifikasi alarm |
| `AlarmService.kt`, `AlarmActivity.kt` | Suara/getar dan layar alarm penuh |
| `Receivers.kt` | Penerima alarm, tombol notifikasi, dan boot |
| `MainActivity.kt`, `AlarmSettingActivity.kt` | Layar utama dan menu alarm |
