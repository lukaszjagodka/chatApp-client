package com.example.chatapp_client.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp_client.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class ConversationActivity extends AppCompatActivity {
    String name, convName;
    int userId;
    private WebSocket webSocket;
    private String SERVER_PATH = "";
    private EditText messageEdit;
    private View sendBtn, pickImgBtn;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);


//        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
//            if(extras == null) {
//                userId = 0;
//                name = null;
//                convName = null;
//            } else {
                userId = extras.getInt("userId");
                name = extras.getString("name");
                convName = extras.getString("conversationName");
//            }
//        } else {
//            userId = (Integer) savedInstanceState.getSerializable("userId");
//            name = (String) savedInstanceState.getSerializable("name");
//            convName = (String) savedInstanceState.getSerializable("conversationName");
//        }
        setTitle(name);
//        TextView textView = findViewById(R.id.ccoz);
//        textView.setText(convName);
        initiateSocketConnection();
    }

    private void initiateSocketConnection() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, )
    }
}