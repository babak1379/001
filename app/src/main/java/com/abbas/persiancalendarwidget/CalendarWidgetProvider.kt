package com.abbas.persiancalendarwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
        scheduleNextMinute(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
            scheduleNextMinute(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.abbas.persiancalendarwidget.ACTION_REFRESH"
        private const val PREFS = "widget_settings"

        private val persianMonths = arrayOf("فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند")
        private val gregorianMonths = arrayOf("ژانویه","فوریه","مارس","آوریل","مه","ژوئن","ژوئیه","اوت","سپتامبر","اکتبر","نوامبر","دسامبر")
        private val hijriMonths = arrayOf("محرم","صفر","ربیع‌الاول","ربیع‌الثانی","جمادی‌الاول","جمادی‌الثانی","رجب","شعبان","رمضان","شوال","ذی‌القعده","ذی‌الحجه")
        private val weekdays = arrayOf("یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه","شنبه")

        private data class CalendarEvent(val title: String, val officialHoliday: Boolean = false)

        // Fixed Solar Hijri dates. Lunar religious holidays need annual official-date verification.
        private val solarEvents = mapOf(
            "1-1" to listOf(CalendarEvent("آغاز نوروز", true)),
            "1-2" to listOf(CalendarEvent("عید نوروز", true)),
            "1-3" to listOf(CalendarEvent("عید نوروز", true)),
            "1-4" to listOf(CalendarEvent("عید نوروز", true)),
            "1-12" to listOf(CalendarEvent("روز جمهوری اسلامی ایران", true)),
            "1-13" to listOf(CalendarEvent("روز طبیعت", true)),
            "3-14" to listOf(CalendarEvent("رحلت امام خمینی", true)),
            "3-15" to listOf(CalendarEvent("قیام ۱۵ خرداد", true)),
            "9-7" to listOf(CalendarEvent("روز نیروی دریایی ارتش")),
            "8-13" to listOf(
                CalendarEvent("روز دانش‌آموز"),
                CalendarEvent("روز ملی مبارزه با استکبار جهانی")
            ),
            "11-22" to listOf(CalendarEvent("پیروزی انقلاب اسلامی", true)),
            "12-29" to listOf(CalendarEvent("ملی شدن صنعت نفت ایران", true))
        )

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
                .forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val now = Date()
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val color = prefs.getInt("text_color", Color.CYAN)
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            val gregorian = Calendar.getInstance().apply { time = now }
            val persian = IcuCalendar.getInstance(ULocale.forLanguageTag("fa-IR-u-ca-persian")).apply { time = now }
            val hijri = IslamicCalendar().apply { time = now }

            views.setTextViewText(R.id.clockText, SimpleDateFormat("HH:mm", Locale.US).format(now))
            views.setTextViewText(R.id.weekdayText, weekdays[gregorian.get(Calendar.DAY_OF_WEEK) - 1])

            val persianMonth = persian.get(Calendar.MONTH) + 1
            val persianDay = persian.get(Calendar.DAY_OF_MONTH)
            val events = solarEvents["$persianMonth-$persianDay"].orEmpty()
            val eventText = events.joinToString("\n") { event ->
                if (event.officialHoliday) "${event.title} (تعطیل رسمی)" else event.title
            }
            views.setTextViewText(R.id.eventsText, eventText)

            views.setTextViewText(R.id.pMonth, persianMonths[persian.get(Calendar.MONTH)])
            views.setTextViewText(R.id.pYear, persian.get(Calendar.YEAR).toString())
            views.setTextViewText(R.id.pMonthNum, persianMonth.toString())
            views.setTextViewText(R.id.pDay, persianDay.toString())

            views.setTextViewText(R.id.gMonth, gregorianMonths[gregorian.get(Calendar.MONTH)])
            views.setTextViewText(R.id.gYear, gregorian.get(Calendar.YEAR).toString())
            views.setTextViewText(R.id.gMonthNum, (gregorian.get(Calendar.MONTH) + 1).toString())
            views.setTextViewText(R.id.gDay, gregorian.get(Calendar.DAY_OF_MONTH).toString())

            views.setTextViewText(R.id.hMonth, hijriMonths[hijri.get(Calendar.MONTH)])
            views.setTextViewText(R.id.hYear, hijri.get(Calendar.YEAR).toString())
            views.setTextViewText(R.id.hMonthNum, (hijri.get(Calendar.MONTH) + 1).toString())
            views.setTextViewText(R.id.hDay, hijri.get(Calendar.DAY_OF_MONTH).toString())

            intArrayOf(
                R.id.clockText, R.id.weekdayText, R.id.eventsText,
                R.id.pMonth, R.id.pYear, R.id.pMonthNum, R.id.pDay,
                R.id.gMonth, R.id.gYear, R.id.gMonthNum, R.id.gDay,
                R.id.hMonth, R.id.hYear, R.id.hMonthNum, R.id.hDay
            ).forEach { views.setTextColor(it, color) }

            val settingsIntent = Intent(context, ColorSettingsActivity::class.java)
            val settingsPendingIntent = PendingIntent.getActivity(
                context, 0, settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, settingsPendingIntent)
            manager.updateAppWidget(id, views)
        }

        private fun scheduleNextMinute(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CalendarWidgetProvider::class.java).setAction(ACTION_REFRESH)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC,
                (System.currentTimeMillis() / 60000L + 1L) * 60000L,
                pendingIntent
            )
        }
    }
}
