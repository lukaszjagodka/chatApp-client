package com.example.chatapp_client.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.Editable;
import android.text.Html;
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
    String conversationName;
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
        messengerDB.execSQL("CREATE TABLE IF NOT EXISTS addedUsers (id INTEGER PRIMARY KEY, userId INTEGER, name VARCHAR, conversationName VARCHAR, timestamp INTEGER)");

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
            if(itemString.length() > 0){
                int tsLong = (int) (System.currentTimeMillis()/1000);
                int index = itemString.indexOf('=');
                String userIdFromDb = itemString.substring(0, index);
                int indexForId = userIdFromDb.indexOf('.');
                String StrNormalIdWthDot = userIdFromDb.substring(0, indexForId);
                int IntNormalIdWthDot = Integer.parseInt(StrNormalIdWthDot);

                String nameAddedUser = itemString.substring(index+1);
                Date date = new Date();
                System.out.println(new Timestamp(date.getTime()));
                boolean mm = isUserExistInDb(IntNormalIdWthDot);
                if(mm){
                    contactListView.setVisibility(View.GONE);
                    searchText.setText("");
                    addedUsersListView.setVisibility(View.VISIBLE);
                    findedUsersLabel.setVisibility(View.INVISIBLE);

                    MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
                    addedUsersListView.setAdapter(addedUsersAdapter);
                    addedUsersAdapter.notifyDataSetChanged();

                    updateListView();
                    if(sizeOfArray()>0){
                        for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
                            FindedUser name = entry.getValue();
                            if(IntNormalIdWthDot == name.getId()){
                                goToConversation(name.getId(), name.getName(), name.getConversationName());
                            }
                        }
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }else{
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", _appPrefs.getEmail());
                    map.put("recipient", StrNormalIdWthDot);
                    client.getServie().executeFindConvName(map).enqueue(new Callback<FindedConverName>() {
                        @Override
                        public void onResponse(Call<FindedConverName> call, Response<FindedConverName> response) {
                            if (response.code() == 200) {
                                FindedConverName result = response.body();
                                assert result != null;
                                System.out.println(result.getConversationName());
                                conversationName = result.getConversationName();

                                String sql = "INSERT INTO addedUsers (userId, name, conversationName, timestamp) VALUES (?,?,?,?)";
                                SQLiteStatement statement = messengerDB.compileStatement(sql);
                                statement.bindString(1, StrNormalIdWthDot);
                                statement.bindString(2, nameAddedUser);
                                statement.bindString(3, conversationName);
                                statement.bindString(4, String.valueOf(tsLong));
                                statement.execute();

                                contactListView.setVisibility(View.GONE);
                                searchText.setText("");
                                addedUsersListView.setVisibility(View.VISIBLE);
                                findedUsersLabel.setVisibility(View.INVISIBLE);
                                updateListView();
                                if(sizeOfArray()>0){
                                    for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
                                        FindedUser name = entry.getValue();
                                        if(IntNormalIdWthDot == name.getId()){
                                            goToConversation(name.getId(), name.getName(), conversationName);
                                        }
                                    }
                                }
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                    Activity.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }else if (response.code() == 400) {
                                Toast.makeText(SearchActivity.this,  response.message(), Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<FindedConverName> call, Throwable t) {
                            Toast.makeText(SearchActivity.this,  t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        // go to conversation
        addedUsersListView.setOnItemClickListener((parent, view, position, id) -> {
            if(sizeOfArray()>0){
                int count=0;
                for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
                    FindedUser name = entry.getValue();
                    System.out.println("testttt "+name.getConversationName());
                    if(id == count){
                        goToConversation(name.getId(), name.getName(), name.getConversationName());
                    }
                    count++;
                }
            }
        });
        // delete record
        addedUsersListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String chosenUser = "";
            int count = 0;
            for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
                if(id == count) {
                    chosenUser = entry.getValue().getName();
                }
                count++;
            }
            new android.app.AlertDialog.Builder(SearchActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete conversation?")
                .setMessage(Html.fromHtml("\t\t\t\t\t\t"+"With "+"<b>"+chosenUser+"</b>", Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("Yes", (dialog, which) -> {
                    Integer itemToDelete=0;
                    int count1 = 0;
                    for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
                        if(id == count1) {
                            itemToDelete = entry.getKey();
                        }
                        count1++;
                    }
                    boolean isItem = deleteFromDb(itemToDelete); //send timestamp
                    if(isItem) {
                        helperMap.remove(itemToDelete);
                        updateListView();
                        MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
                        addedUsersListView.setAdapter(addedUsersAdapter);
                        addedUsersAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("No", null)
                .show();
            return true;
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
    public boolean deleteFromDb(Integer itemToDelete){
        System.out.println(itemToDelete);
        for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
            if(itemToDelete.equals(entry.getKey())){
                String sql = "DELETE FROM addedUsers WHERE timestamp = "+itemToDelete;
                System.out.println(sql);
                SQLiteStatement statement = messengerDB.compileStatement(sql);
                statement.execute();
                return true;
            }
        }
        return false;
    }
    public void updateListView() {
        Cursor c = messengerDB.rawQuery("SELECT * FROM addedUsers", null);
        userIndex = c.getColumnIndex("userId");
        int nameIndex = c.getColumnIndex("name");
        int converId = c.getColumnIndex("conversationName");
        int timeStamp = c.getColumnIndex("timestamp");
        addedUsersMap.clear();
        if (c.moveToFirst()) {
            do {
                int intUserId = c.getInt(userIndex);
                String nameUs = c.getString(nameIndex);
                String strConversationName = c.getString(converId);
                int intTimestamp = c.getInt(timeStamp);
                int index = nameUs.indexOf(',');
                String strNameAddedUser = nameUs.substring(index + 1);
                addedUsersMap.put(intTimestamp, new FindedUser(intUserId, strNameAddedUser, strConversationName, intTimestamp));
            } while (c.moveToNext());
        }
        Map<Integer, FindedUser> reverseSortedMap = new TreeMap<>(Collections.reverseOrder());
        reverseSortedMap.putAll(addedUsersMap);
        for (Map.Entry<Integer, FindedUser> entry : reverseSortedMap.entrySet()) {
            FindedUser user = entry.getValue();
            helperMap.put(entry.getKey(), new FindedUser(user.getId(), user.getName(), user.getConversationName(), user.getTimestamp()));
        }
        MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
        addedUsersListView.setAdapter(addedUsersAdapter);
        addedUsersAdapter.notifyDataSetChanged();
    }
    public boolean isUserExistInDb(int userIdFromDb){
        System.out.println(userIdFromDb);
        if(addedUsersMap.size() == 0) {
            return false;
        }else{
            Cursor c = messengerDB.rawQuery("SELECT * FROM addedUsers", null);
            userIndex = c.getColumnIndex("userId");
            if (c.moveToFirst()) {
                do {
                    int intUserId = c.getInt(userIndex);
                    if (intUserId == userIdFromDb) {
                        int tsLong = (int) (System.currentTimeMillis()/1000);
                        String sql = "UPDATE addedUsers SET timestamp = "+tsLong+" WHERE userId = "+userIdFromDb;
                        SQLiteStatement statement = messengerDB.compileStatement(sql);
                        statement.execute();
                        return true;
                    }
                } while (c.moveToNext());
            }
        }
        return false;
    }
    public int sizeOfArray() {
        int aaa=0;
        Cursor c = messengerDB.rawQuery("SELECT * FROM addedUsers", null);
        if (c.moveToFirst()) {
            do {
                aaa++;
            } while (c.moveToNext());
        }
        System.out.println("sizeOfArray: "+ aaa);
        return aaa;
    }
    public void goToConversation(int userId, String userName, String conversationName){
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("name", userName);
        intent.putExtra("conversationName", conversationName);
        startActivity(intent);
    }
}