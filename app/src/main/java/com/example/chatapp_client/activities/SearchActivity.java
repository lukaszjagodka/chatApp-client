package com.example.chatapp_client.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;

public class SearchActivity extends AppCompatActivity {
    private AppPreferences _appPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        _appPrefs = new AppPreferences(getApplicationContext());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_chpassword) {
            return true;
        }
        if (id == R.id.action_logout) {
            _appPrefs.saveRemember(false);
            _appPrefs.saveIsToken(false);
            _appPrefs.saveName("");
            _appPrefs.saveToken("");
            _appPrefs.saveEmail("");
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
        }
}