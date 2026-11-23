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
        fun updateAll(context: Context) {
            val m = AppWidgetManager.getInstance(context)
            m.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java)).forEach { updateOne(context,m,it) }
        }
        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val now = Date()
            val color = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("text_color", Color.WHITE)
            val v = RemoteViews(context.packageName, R.layout.widget_calendar)
            val g = Calendar.getInstance().apply { time = now }
            val p = IcuCalendar.getInstance(ULocale.forLanguageTag("fa-IR-u-ca-persian")).apply { time = now }
            val h = IslamicCalendar().apply { time = now }
            v.setTextViewText(R.id.clockText, SimpleDateFormat("HH:mm", Locale.US).format(now))
            v.setTextViewText(R.id.weekdayText, weekdays[g.get(Calendar.DAY_OF_WEEK)-1])
            v.setTextViewText(R.id.pMonth, persianMonths[p.get(Calendar.MONTH)])
            v.setTextViewText(R.id.pYear, p.get(Calendar.YEAR).toString())
            v.setTextViewText(R.id.pMonthNum, (p.get(Calendar.MONTH)+1).toString())
            v.setTextViewText(R.id.pDay, p.get(Calendar.DAY_OF_MONTH).toString())
            v.setTextViewText(R.id.gMonth, gregorianMonths[g.get(Calendar.MONTH)])
            v.setTextViewText(R.id.gYear, g.get(Calendar.YEAR).toString())
            v.setTextViewText(R.id.gMonthNum, (g.get(Calendar.MONTH)+1).toString())
            v.setTextViewText(R.id.gDay, g.get(Calendar.DAY_OF_MONTH).toString())
            v.setTextViewText(R.id.hMonth, hijriMonths[h.get(Calendar.MONTH)])
            v.setTextViewText(R.id.hYear, h.get(Calendar.YEAR).toString())
            v.setTextViewText(R.id.hMonthNum, (h.get(Calendar.MONTH)+1).toString())
            v.setTextViewText(R.id.hDay, h.get(Calendar.DAY_OF_MONTH).toString())
            intArrayOf(R.id.clockText,R.id.weekdayText,R.id.pMonth,R.id.pYear,R.id.pMonthNum,R.id.pDay,R.id.gMonth,R.id.gYear,R.id.gMonthNum,R.id.gDay,R.id.hMonth,R.id.hYear,R.id.hMonthNum,R.id.hDay).forEach { v.setTextColor(it,color) }
            val pi = PendingIntent.getActivity(context,0,Intent(context,ColorSettingsActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            v.setOnClickPendingIntent(R.id.widgetRoot,pi)
            manager.updateAppWidget(id,v)
        }
        private fun scheduleNextMinute(context: Context) {
            val a = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(context,CalendarWidgetProvider::class.java).setAction(ACTION_REFRESH)
            val p = PendingIntent.getBroadcast(context,1,i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            a.setAndAllowWhileIdle(AlarmManager.RTC,(System.currentTimeMillis()/60000L+1L)*60000L,p)
        }
    }
}
