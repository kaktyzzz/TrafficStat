﻿package com.example.trafficstat;

import java.util.Arrays;
import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.sax.StartElementListener;
import android.util.Log;
import android.widget.RemoteViews;
import android.net.TrafficStats;
import android.os.Bundle;
import android.provider.Settings;

public class TrafficStat extends AppWidgetProvider {

  public static final String LOG_TAG = "TraficWidget";
  static final int metricDel = 1048576;
  static final String metric = "MB";
  static final String ACTION_OPENSETTINGS = "com.example.trafficstat.openSettings";
  
  public static int maxTraffic = 3072;
  

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
    Log.d(LOG_TAG, "onEnabled");
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);
    Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));	
    for (int id : appWidgetIds) {
    	updateWidget(context, appWidgetManager, id);
    }
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    super.onDeleted(context, appWidgetIds);
    Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
  }

  @Override
  public void onDisabled(Context context) {
    super.onDisabled(context);
    Log.d(LOG_TAG, "onDisabled");
  }

  static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
	  Log.d(LOG_TAG, "updateWidget " + widgetID);
	  
	  // читаем Preferences
	  SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
	  int modei = sp.getInt(ConfigActivity.WIDGET_MODEI + widgetID, -1);
	  String widgetMode = sp.getString(ConfigActivity.WIDGET_MODES + widgetID, null);
	  float currentDataSize = sp.getFloat(ConfigActivity.WIDGET_CURRENT_DATA_SIZE + widgetID, 0);
	  float realDataSize = sp.getFloat(ConfigActivity.WIDGET_REAL_DATA_SIZE + widgetID, 0);
	  if (widgetMode == null) return;
	  
	  long statMob = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
	  long statTotal = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
	  float stat = 0;
	  switch (modei) {
	  	case 0:
	  		stat =  (float)(statTotal - statMob) / metricDel;
	  		break;
	  	case 1:
	  		stat = (float)(statMob) / metricDel;
	  		break;
	  	case -1:
	  		return;
	  }
	  
	  if (currentDataSize > stat)
		  realDataSize += currentDataSize;
	  
	  currentDataSize = stat;
	  stat += realDataSize;
	  
	  Editor editor = sp.edit();
	  editor.putFloat(ConfigActivity.WIDGET_CURRENT_DATA_SIZE + widgetID, currentDataSize);
	  editor.putFloat(ConfigActivity.WIDGET_REAL_DATA_SIZE + widgetID, realDataSize);
	  editor.commit();
	  
	  // Настраиваем внешний вид виджета
	  RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
	  widgetView.setTextViewText(R.id.tv, widgetMode + ": " + String.format("%.02f", stat) + " " + metric);
	  
	  //считаем % съеденого трафика
	  float trafficPrec = stat / maxTraffic * 100;
	  Log.d(LOG_TAG, "% traffic "+trafficPrec);
	  
	  //считаем % пройденных дней
	  Calendar calendar = (Calendar) Calendar.getInstance().clone();
	  int y = calendar.get(Calendar.YEAR);
	  int m = calendar.get(Calendar.MONTH);
	  int d = calendar.get(Calendar.DAY_OF_MONTH);
	  calendar.set(y, m, 1);
	  int max_d = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	  
	  float timePerc = (float)d / max_d * 100;
	  Log.d(LOG_TAG, "% time "+timePerc+" d:"+d+" max_d:"+max_d);
	  
	  widgetView.setInt(R.id.progressBar1, "setProgress", (int)trafficPrec);
	  widgetView.setInt(R.id.progressBar1, "setSecondaryProgress", (int)timePerc);
	    
	  //Нажатие на 1 зону
	  Intent updateIntent = new Intent(context, TrafficStat.class);
	  updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	  updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
	  PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, updateIntent, 0);
	  widgetView.setOnClickPendingIntent(R.id.l, pIntent);
	  //Нажатие на 2 зону
	  Intent countIntent = new Intent(context, TrafficStat.class);
	  countIntent.setAction(ACTION_OPENSETTINGS);
	  countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
	  pIntent = PendingIntent.getBroadcast(context, widgetID, countIntent, 0);
	  widgetView.setOnClickPendingIntent(R.id.openSettings, pIntent);

	  
	  
	  // Обновляем виджет
	  appWidgetManager.updateAppWidget(widgetID, widgetView);
  }
  
  public void onReceive(Context context, Intent intent) {
	  super.onReceive(context, intent);
	  
	  Log.d(LOG_TAG, "OnReceive");
	  int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	  Bundle extras = intent.getExtras();
	  if (extras != null) {
		  mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	  } 
	  updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetId);
	  
	  // Проверяем, что это intent от нажатия на 2 зону
	  if (intent.getAction().equalsIgnoreCase(ACTION_OPENSETTINGS)) {
		  try {
			  //вызываем окно настроек
			  Intent in = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			  in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  context.startActivity(in);
		  } catch (Exception e) {
			  Log.e(LOG_TAG, e.getMessage());
		  }
	  }
  }
  
}