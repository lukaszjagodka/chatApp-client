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
        String convName;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                userId = 0;
                name = null;
                convName = null;
            } else {
                userId = extras.getInt("userId");
                name = extras.getString("name");
                convName = extras.getString("conversationName");
            }
        } else {
            userId = (Integer) savedInstanceState.getSerializable("userId");
            name = (String) savedInstanceState.getSerializable("name");
            convName = (String) savedInstanceState.getSerializable("conversationName");
        }
        setTitle(name);
//        TextView textView = findViewById(R.id.ccoz);
//        textView.setText(convName);
    }
}