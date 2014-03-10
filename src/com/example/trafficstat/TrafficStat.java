package com.example.trafficstat;

import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.net.TrafficStats;

public class TrafficStat extends AppWidgetProvider {

  static final String LOG_TAG = "myLogs";
  static final int metric = 1048576;

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
    Log.d(LOG_TAG, "onEnabled");
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);
    Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
    SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
    for (int id : appWidgetIds) {
    	updateWidget(context, appWidgetManager, sp, id);
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

  static void updateWidget(Context context, AppWidgetManager appWidgetManager, SharedPreferences sp, int widgetID) {
	  Log.d(LOG_TAG, "updateWidget " + widgetID);
	    
	  // Читаем параметры Preferences
	  int modei = sp.getInt(ConfigActivity.WIDGET_MODEI, -1);
	  String widgetMode = sp.getString(ConfigActivity.WIDGET_MODES + widgetID, null);
	  if (widgetMode == null) return;
	  
	  long statMob = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
	  long statTotal = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
	  float stat = 0;
	  switch (modei) {
	  	case 0:
	  		stat =  (float)(statTotal - statMob) / metric;
	  		break;
	  	case 1:
	  		stat = (float)(statMob) / metric;
	  		break;
	  	case -1:
	  		return;
	  }
	  
	  // Настраиваем внешний вид виджета
	  RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
	  widgetView.setTextViewText(R.id.tv, widgetMode + ": " + String.format("%.03f", stat));
	    
	  // Обновляем виджет
	  appWidgetManager.updateAppWidget(widgetID, widgetView);
  }
  
}