package com.example.chatapp_client.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.retrofit.RetrofitClient;
import com.example.chatapp_client.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.sql.Timestamp;
import java.util.*;

public class SearchActivity extends AppCompatActivity {
    public AppPreferences _appPrefs;
    RetrofitClient client = new RetrofitClient();
    ListView addedUsersListView, contactListView;
    ArrayList<Object> contactsUsers = new ArrayList<>();
    HashMap<String, String> recivedUsers = new HashMap<>();
    Map<Integer, FindedUser> addedUsersMap = new LinkedHashMap<>();
    Map<Integer, FindedUser> helperMap = new LinkedHashMap<>();
    int userIndex;
    SQLiteDatabase messengerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle("CommisionaireChat");
        _appPrefs = new AppPreferences(getApplicationContext());

        contactListView = findViewById(R.id.searchView);
        addedUsersListView = findViewById(R.id.addedUsers);
        EditText searchText = findViewById(R.id.searchText);
        TextView findedUsersLabel = findViewById(R.id.findedUsersLabel);
        HashMap<String, String> searchDataMap = new HashMap<>();

        messengerDB = this.openOrCreateDatabase("CommisionaireDB", MODE_PRIVATE, null);
        messengerDB.execSQL("CREATE TABLE IF NOT EXISTS addedUsers (id INTEGER PRIMARY KEY, userId INTEGER, name VARCHAR, timestamp INTEGER)");

//        deleteTable();
        updateListView();

        // seach user in base
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchDataMap.put("name", s.toString());
                searchDataMap.put("more", "1");
                client.getServie().executeSearchUser(searchDataMap).enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                        if (response.code() == 200) {
                            SearchResult searchResult = response.body();
                            assert searchResult != null;
                            contactsUsers = searchResult.getFindedUsers();
                            if(contactsUsers != null){
                                if(contactsUsers.size() > 0){
                                    JSONArray jsonarray = new JSONArray(contactsUsers);
                                    try {
                                        for(int i=0;i < jsonarray.length();i++) {
                                            JSONObject e = jsonarray.getJSONObject(i);
                                            recivedUsers.put(e.getString("id"), e.getString("name"));
                                        }
                                        MyAdapter arrayAdapter = new MyAdapter(recivedUsers);
                                        contactListView.setAdapter(arrayAdapter);
                                    } catch (JSONException e) {
                                        Log.e("log_tag", "Error parsing data "+e.toString());
                                    }
                                }
                            }
                        }else if (response.code() == 400) {
                            Toast.makeText(SearchActivity.this,  response.message(), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<SearchResult> call, Throwable t) {
                        Toast.makeText(SearchActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void afterTextChanged(Editable s) {
                recivedUsers.clear();
                MyAdapter arrayAdapter = new MyAdapter(recivedUsers);
                arrayAdapter.notifyDataSetChanged();
                contactListView.setAdapter(arrayAdapter);
                contactListView.setVisibility(View.VISIBLE);
                addedUsersListView.setVisibility(View.INVISIBLE);
                findedUsersLabel.setVisibility(View.VISIBLE);
            }
        });
        // chose and add user to addedUser list
        contactListView.setOnItemClickListener((parent, view, position, id) -> {
            Object item = contactListView.getItemAtPosition(position);
            String itemString = String.valueOf(item);
            if(itemString.length() > 0) {
                int tsLong = (int) (System.currentTimeMillis()/1000);
                int index = itemString.indexOf('=');
                String userIdFromDb = itemString.substring(0, index);
                int indexForId = userIdFromDb.indexOf('.');
                String StrNormalIdWthDot = userIdFromDb.substring(0, indexForId);
                int IntNormalIdWthDot = Integer.parseInt(StrNormalIdWthDot);

                String nameAddedUser = itemString.substring(index+1);
                Date date = new Date();
                String sql = "INSERT INTO addedUsers (userId, name, timestamp) VALUES (?,?,?)";
                SQLiteStatement statement = messengerDB.compileStatement(sql);
                statement.bindString(1, StrNormalIdWthDot);
                statement.bindString(2, nameAddedUser);
                statement.bindString(3, String.valueOf(tsLong));
                statement.execute();
                contactListView.setVisibility(View.GONE);
                searchText.setText("");
                addedUsersListView.setVisibility(View.VISIBLE);
                findedUsersLabel.setVisibility(View.INVISIBLE);

                MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
                addedUsersListView.setAdapter(addedUsersAdapter);
                addedUsersAdapter.notifyDataSetChanged();

                updateListView();
                InputMethodManager imm = (InputMethodManager) getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_chpassword) {
            View view = getLayoutInflater().inflate(R.layout.change_pass_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setNegativeButton(getString(android.R.string.cancel),
                (dialog, which) -> dialog.dismiss());
            builder.setView(view).show();

            Button changePassBtn = view.findViewById(R.id.changePassBtn);
            EditText actualPass = view.findViewById(R.id.actualPass);
            EditText newPass = view.findViewById(R.id.newPass);
            String strNewPass = newPass.getText().toString();

            EditText newPassRep = view.findViewById(R.id.newPassRep);
            String strNewPassRep = newPassRep.getText().toString();
            changePassBtn.setOnClickListener(v -> {
                boolean verActualPass = Helpers.validationPassword(actualPass, SearchActivity.this);
                boolean verPassOne = Helpers.validationPassword(newPass, SearchActivity.this);
                boolean verPassTwo = Helpers.validationPassword(newPassRep, SearchActivity.this);
                if(!verActualPass || !verPassOne || !verPassTwo){
                    Toast.makeText(SearchActivity.this, "Check your passwords.", Toast.LENGTH_SHORT).show();
                }else{
                    if(!(strNewPass.equals(strNewPassRep) || (strNewPass.length() == 0) || (strNewPassRep.length() == 0))){
                        Toast.makeText(SearchActivity.this, "New passwords are different.", Toast.LENGTH_SHORT).show();
                    }else{
                        String token = _appPrefs.getToken();
                        String email = _appPrefs.getEmail();

                        HashMap<String, String> map = new HashMap<>();
                        map.put("actualPass", actualPass.getText().toString());
                        map.put("newPassword", newPass.getText().toString());
                        map.put("email", email);
                        String authToken = "Bearer "+ token;

                        client.getServie().executePasswordChange(authToken, map).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if(response.code() == 200){
                                    Toast.makeText(SearchActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                                    logout();
                                }else if(response.code() == 401){
                                    Toast.makeText(SearchActivity.this, "Password was not changed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(SearchActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
            return super.onOptionsItemSelected(item);
        }

    public void logout(){
        _appPrefs.saveRemember(false);
        _appPrefs.saveIsToken(false);
        _appPrefs.saveName("");
        _appPrefs.saveToken("");
        _appPrefs.saveEmail("");
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
        }
    public void deleteTable(){
        messengerDB.execSQL("DELETE FROM addedUsers");
//        messengerDB.execSQL("DROP TABLE addedUsers");
        System.out.println("Delete table");
    }
    public void updateListView() {
        Cursor c = messengerDB.rawQuery("SELECT * FROM addedUsers", null);
        userIndex = c.getColumnIndex("userId");
        int nameIndex = c.getColumnIndex("name"); //??
        int timeStamp = c.getColumnIndex("timestamp");
        addedUsersMap.clear();
        if (c.moveToFirst()) {
            do {
                int intUserId = c.getInt(userIndex);
                String nameUs = c.getString(nameIndex);
                int intTimestamp = c.getInt(timeStamp);
                int index = nameUs.indexOf(',');
                String strNameAddedUser = nameUs.substring(index + 1);
                addedUsersMap.put(intTimestamp, new FindedUser(intUserId, strNameAddedUser, intTimestamp));
            } while (c.moveToNext());
        }
        Map<Integer, FindedUser> reverseSortedMap = new TreeMap<>(Collections.reverseOrder());
        reverseSortedMap.putAll(addedUsersMap);
        for (Map.Entry<Integer, FindedUser> entry : reverseSortedMap.entrySet()) {
            FindedUser user = entry.getValue();
            helperMap.put(entry.getKey(), new FindedUser(user.getId(), user.getName(), user.getTimestamp()));
        }
        MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
        addedUsersListView.setAdapter(addedUsersAdapter);
        addedUsersAdapter.notifyDataSetChanged();
    }
}