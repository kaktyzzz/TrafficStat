﻿package com.example.trafficstat;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

public class ConfigActivity extends Activity {

  int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
  Intent resultValue;

  public final static String WIDGET_PREF				= "widget_pref";
  public final static String WIDGET_MODES 	 			= "widget_modes_";
  public final static String WIDGET_MODEI 				= "widget_modei_";
  public final static String WIDGET_CURRENT_DATA_SIZE 	= "widget_current_data_size_";
  public final static String WIDGET_REAL_DATA_SIZE 		= "widget_real_data_size_";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TrafficStat.LOG_TAG, "onCreate config");
    
    // извлекаем ID конфигурируемого виджета
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    // и проверяем его корректность
    if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
    }
    
    // формируем intent ответа
    resultValue = new Intent();
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
    
    // отрицательный ответ
    setResult(RESULT_CANCELED, resultValue);
    
    setContentView(R.layout.config);
  }
  
  
  public void onClick(View v) {
    Spinner spn = ((Spinner) findViewById(R.id.spinner1));
	String modes = spn.getSelectedItem().toString();
	int modei = spn.getSelectedItemPosition();
	

    // пишем в Preferences
    SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
    Editor editor = sp.edit();
    editor.putString(WIDGET_MODES + widgetID, modes);
    editor.putInt(WIDGET_MODEI + widgetID, modei);
    editor.commit();

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    TrafficStat.updateWidget(this, appWidgetManager, widgetID);
    
    // положительный ответ 
    setResult(RESULT_OK, resultValue);
      
    Log.d(TrafficStat.LOG_TAG, "finish config " + widgetID);
    finish();
  }
}

