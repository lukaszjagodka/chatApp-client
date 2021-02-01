package com.example.chatapp_client.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.utils.MessageAdapter;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TimerTask;

public class ConversationActivity extends AppCompatActivity implements TextWatcher {
    public AppPreferences _appPrefs;
    String myName, convName, name;
    int userId;
    private WebSocket webSocket;
    private EditText messageEdit;
    private View sendBtn, pickImgBtn;
    private final int IMAGE_REQUEST_ID = 1;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Bundle extras = getIntent().getExtras();
        _appPrefs = new AppPreferences(getApplicationContext());

        userId = extras.getInt("userId");
        name = extras.getString("name");
        myName = extras.getString("myName");
        convName = extras.getString("conversationName");
        setTitle(name);

        if (!_appPrefs.getSocketConn()){
            initiateSocketConnection();
        }
    }

    public Runnable initiateSocketConnection() {
        if(!_appPrefs.getSocketConn()) {
            OkHttpClient client = new OkHttpClient();
            String SERVER_PATH = "ws://192.168.100.3:3001";
            Request request = new Request.Builder().url(SERVER_PATH).build();
            webSocket = client.newWebSocket(request, new SocketListener());
            _appPrefs.saveSocketConn(true);
        }
        return null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString().trim();
        if(string.isEmpty()){
            resetMessageEdit();
        }else{
            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void resetMessageEdit() {
        messageEdit.removeTextChangedListener(this);
        messageEdit.setText("");
        sendBtn.setVisibility(View.INVISIBLE);
        pickImgBtn.setVisibility(View.VISIBLE);
        messageEdit.addTextChangedListener(this);
    }

    public class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            System.out.println(response);
            super.onOpen(webSocket, response);
            runOnUiThread(()-> {
                initializeView();

                setTimeout(ConversationActivity.this::ping, 30000);
            });
        }
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);
            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent", false);
                    String userType = jsonObject.getString("type");
                    switch (userType) {
                        case "userping":
                            _appPrefs.saveSocketConn(true);
                            break;
                        case "userevent":
                            System.out.println("message " + text);
                            messageAdapter.addItem(jsonObject);
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
            runOnUiThread(() -> {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "closing");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocket.send(jsonObject.toString());
//                System.out.println("onClosing "+webSocket +" "+code+" "+reason);
            });
        }
        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            runOnUiThread(() -> {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "closed");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocket.send(jsonObject.toString());
//                System.out.println("onClosed "+webSocket +" "+code+" "+reason);
            });
        }
        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
//            System.out.println("onFailure "+webSocket+" "+t+" "+response);
            _appPrefs.saveSocketConn(false);
            setTimeout(initiateSocketConnection(),1000);
        }
    }

    private void initializeView() {
        messageEdit = findViewById(R.id.messageEdit);
        sendBtn = findViewById(R.id.sendBtn);
        pickImgBtn = findViewById(R.id.pickImgBtn);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
//        System.out.println("position "+messageAdapter.getItemCount());
//        recyclerView.scrollToPosition(messageAdapter.getItemCount());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageEdit.addTextChangedListener(this);

        sendBtn.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", "userevent");
                jsonObject.put("name", myName);
                jsonObject.put("convName", convName);
                jsonObject.put("message", messageEdit.getText().toString());

                webSocket.send(jsonObject.toString());
                jsonObject.put("isSent", true);
                messageAdapter.addItem(jsonObject);
                resetMessageEdit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        pickImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pick image"), IMAGE_REQUEST_ID);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK){
            try {
                assert data != null;
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap image = BitmapFactory.decodeStream(is);

                sendImage(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImage(Bitmap image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "userevent");
            jsonObject.put("name", myName);
            jsonObject.put("convName", convName);
            jsonObject.put("image", base64String);

            webSocket.send(jsonObject.toString());

            jsonObject.put("isSent", true);
            messageAdapter.addItem(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    public TimerTask ping(){
        runOnUiThread(()-> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", "userping");
                webSocket.send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        setTimeout(ConversationActivity.this::ping, 30000);
        return null;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        webSocket.close(1000, "closed connection");
        _appPrefs.saveSocketConn(false);
    }
    protected void onPause() {
        super.onPause();
//        System.out.println("onPause");
    }
    protected void onStop() {
        super.onStop();
//        System.out.println("onStop");
    }
    public void onDestroy(){
        super.onDestroy();
//        System.out.println("onDestroy");
        webSocket.close(1000, "closed connection");
        _appPrefs.saveSocketConn(false);
    }
}
