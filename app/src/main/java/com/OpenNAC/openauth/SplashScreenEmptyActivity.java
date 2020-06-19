package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SplashScreenEmptyActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 0;
    private static final String SHARED_PREFS = "sharedPrefs",FINISH = "finish";
    private boolean finished;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        finished = sharedPreferences.getBoolean(FINISH, false);
        setLanguage(sharedPreferences.getString("getLanguage", ""));
        new Handler().postDelayed(() -> {
            if (finished){
                Intent homeIntent = new Intent(SplashScreenEmptyActivity.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            }
            else {
                Intent homeIntent = new Intent(SplashScreenEmptyActivity.this, OnboardingActivity.class);
                startActivity(homeIntent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
    private void setLanguage(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        onConfigurationChanged(configuration);
    }
}
