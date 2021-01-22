package com.example.chatapp_client.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.utils.Helpers;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public AppPreferences _appPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        _appPrefs = new AppPreferences(getApplicationContext());

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},10);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashActivity = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(splashActivity);
                if (Helpers.isAppRunning(MainActivity.this, "com.example.chatapp_client")) {
                    _appPrefs.saveSocketConn(false);
                }
            }
        }, 3000);
    }
}