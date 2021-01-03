package com.example.chatapp_client.activities;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        String name;
        int userId;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                userId = 0;
                name = null;
            } else {
                userId = extras.getInt("userId");
                name = extras.getString("name");
            }
        } else {
            userId = (Integer) savedInstanceState.getSerializable("userId");
            name = (String) savedInstanceState.getSerializable("name");
        }
        setTitle(name);
        TextView textView = findViewById(R.id.ccoz);
        textView.setText(String.valueOf(userId));
    }
}