package com.abbas.persiancalendarwidget

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class ColorSettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("widget_settings", MODE_PRIVATE)
        val colors = listOf("سفید" to Color.WHITE,"زرد" to Color.YELLOW,"فیروزه‌ای" to Color.CYAN,"سبز" to Color.GREEN,"قرمز" to Color.RED,"آبی" to Color.BLUE,"نارنجی" to 0xFFFF9800.toInt(),"صورتی" to 0xFFFF80AB.toInt())
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(24,20,24,20) }
        root.addView(TextView(this).apply { text="رنگ نوشته‌های ویجت را انتخاب کن"; textSize=18f; gravity=Gravity.CENTER; setPadding(8,8,8,16) })
        colors.forEach { (name,color) -> root.addView(Button(this).apply {
            text=name; setTextColor(color)
            setOnClickListener { prefs.edit().putInt("text_color",color).apply(); CalendarWidgetProvider.updateAll(this@ColorSettingsActivity); finish() }
        },LinearLayout.LayoutParams(-1,-2)) }
        setContentView(root)
    }
}
