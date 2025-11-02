package com.example.music.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.music.R

class WidgetPlayerProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_player)

            val playIntent = Intent(context, WidgetControlReceiver::class.java).apply {
                action = "ACTION_TOGGLE_PLAY"
            }
            val playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.button_play_pause, playPendingIntent)

            val nextIntent = Intent(context, WidgetControlReceiver::class.java).apply {
                action = "ACTION_NEXT"
            }
            val nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.button_next, nextPendingIntent)

            val prevIntent = Intent(context, WidgetControlReceiver::class.java).apply {
                action = "ACTION_PREV"
            }
            val prevPendingIntent = PendingIntent.getBroadcast(context, 2, prevIntent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.button_prev, prevPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
