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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.retrofit.RetrofitClient;
import com.example.chatapp_client.utils.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
 String conversationName, tableName;
 EditText searchText;
 TextView findedUsersLabel;
 SQLiteDatabase messengerDB;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_search);
  setTitle("CommisionaireChat");
  _appPrefs = new AppPreferences(getApplicationContext());

  contactListView = findViewById(R.id.searchView);
  addedUsersListView = findViewById(R.id.addedUsers);
  searchText = findViewById(R.id.searchText);
  findedUsersLabel = findViewById(R.id.findedUsersLabel);
  HashMap<String, String> searchDataMap = new HashMap<>();

  String tableUserName = _appPrefs.getEmail();
  tableName = "addedUsers" + tableUserName.substring(0, tableUserName.indexOf("@"));

  messengerDB = this.openOrCreateDatabase("CommisionaireDB", MODE_PRIVATE, null);

  checkMessages();
  // first login on mobile device, if account exist
  if (!isTableExists(tableName)) {
   messengerDB.execSQL("CREATE TABLE IF NOT EXISTS '"+tableName+"' (id INTEGER PRIMARY KEY, userId INTEGER, name VARCHAR, conversationName VARCHAR, timestamp INTEGER)");
   checkContacts(tableName);
  } else { // second and more login times
//    deleteTable(tableName);
    updateListView(tableName);
    checkContacts(tableName);
  }

  // seach user in base
  searchText.addTextChangedListener(new TextWatcher() {
   @Override
   public void beforeTextChanged(CharSequence s, int start, int count, int after) {
   }

   @Override
   public void onTextChanged(CharSequence s, int start, int before, int count) {
    searchDataMap.put("name", s.toString());
    searchDataMap.put("more", "1");
    client.getServie().executeSearchUser(searchDataMap).enqueue(new Callback<SearchResult>() {
     @Override
     public void onResponse(@NonNull Call<SearchResult> call, @NonNull Response<SearchResult> response) {
      SearchResult searchResult = response.body();
      assert searchResult != null;
      contactsUsers = searchResult.getFindedUsers();
      if(contactsUsers == null){
       Log.e("log_tag", "empty array");
      }else{
       JSONArray jsonarray = new JSONArray(contactsUsers);
       try {
        for (int i = 0; i < jsonarray.length(); i++) {
         JSONObject e = jsonarray.getJSONObject(i);
         recivedUsers.put(e.getString("id"), e.getString("name"));
        }
        MyAdapter arrayAdapter = new MyAdapter(recivedUsers);
        contactListView.setAdapter(arrayAdapter);
       } catch (JSONException e) {
        Log.e("log_tag", "Error parsing data " + e.toString());
       }
      }
     }
     @Override
     public void onFailure(@NonNull Call<SearchResult> call, @NonNull Throwable t) {}
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

    if (s.length() == 0 || s == null) {
     arrayAdapter.notifyDataSetChanged();
     contactListView.setVisibility(View.GONE);
     findedUsersLabel.setVisibility(View.INVISIBLE);
     addedUsersListView.setVisibility(View.VISIBLE);
     recivedUsers.clear();
    }
   }
  });
  // chose and add user to addedUser list
  contactListView.setOnItemClickListener((parent, view, position, id) -> {
   Object item = contactListView.getItemAtPosition(position);
   String itemString = String.valueOf(item);
   if (itemString.length() > 0) {
    int tsLong = (int) (System.currentTimeMillis() / 1000);
    int index = itemString.indexOf('=');
    String userIdFromDb = itemString.substring(0, index);
    int indexForId = userIdFromDb.indexOf('.');
    String StrNormalIdWthDot = userIdFromDb.substring(0, indexForId);
    int IntNormalIdWthDot = Integer.parseInt(StrNormalIdWthDot);

    String nameAddedUser = itemString.substring(index + 1);

    //add new contact to postgresDb
    String jwt = _appPrefs.getToken();
    String authToken = "Bearer " + jwt;
    HashMap<String, String> saveUserToDb = new HashMap<>();
    saveUserToDb.put("email", _appPrefs.getEmail());
    saveUserToDb.put("addedUserId", StrNormalIdWthDot);

    client.getServie().executeAddUserToContactList(authToken, saveUserToDb).enqueue(new Callback<Void>() {
     @Override
     public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                        System.out.println("User added to base");
     }

     @Override
     public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
//                        Toast.makeText(SearchActivity.this,  t.getMessage(), Toast.LENGTH_LONG).show();
     }
    });

    boolean mm = isUserExistInDb(IntNormalIdWthDot, tableName);
    if (mm) {
     fncAddIfUserExist(IntNormalIdWthDot, tableName);
//     contactListView.setVisibility(View.GONE);
//     searchText.setText("");
//     addedUsersListView.setVisibility(View.VISIBLE);
//     findedUsersLabel.setVisibility(View.INVISIBLE);
//
//     MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
//     addedUsersListView.setAdapter(addedUsersAdapter);
//     addedUsersAdapter.notifyDataSetChanged();
//
//     updateListView(tableName);
//     if (sizeOfArray(tableName) > 0) {
//      for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
//       FindedUser name = entry.getValue();
//       if (IntNormalIdWthDot == name.getId()) {
//        goToConversation(name.getId(), name.getName(), name.getConversationName());
//       }
//      }
//     }
//     InputMethodManager imm = (InputMethodManager) getSystemService(
//       Activity.INPUT_METHOD_SERVICE);
//     imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    } else {
     HashMap<String, String> map = new HashMap<>();
     map.put("email", _appPrefs.getEmail());
     map.put("recipient", StrNormalIdWthDot);
     client.getServie().executeFindConvName(map).enqueue(new Callback<FindedConverName>() {
      @Override
      public void onResponse(@NonNull Call<FindedConverName> call, @NonNull Response<FindedConverName> response) {
//       fncAddIfUserNotExist(tsLong, StrNormalIdWthDot, nameAddedUser, IntNormalIdWthDot);
//       if (response.code() == 200) {

        FindedConverName result = response.body();
        assert result != null;
        conversationName = result.getConversationName();

        String sql = "INSERT INTO " + tableName + " (userId, name, conversationName, timestamp) VALUES (?,?,?,?)";
        SQLiteStatement statement = messengerDB.compileStatement(sql);
        statement.bindString(1, StrNormalIdWthDot);
        statement.bindString(2, nameAddedUser);
        statement.bindString(3, conversationName);
        statement.bindString(4, String.valueOf(tsLong));
        statement.execute();

        String convNameFP = conversationName.substring(0, 8);
        String convNameLastV = "conv"+convNameFP;
        messengerDB.execSQL("CREATE TABLE IF NOT EXISTS '" + convNameLastV + "' (id INTEGER PRIMARY KEY, name VARCHAR, message VARCHAR, isSent BOOLEAN, timestamp INTEGER)");

        contactListView.setVisibility(View.GONE);
        searchText.setText("");
        addedUsersListView.setVisibility(View.VISIBLE);
        findedUsersLabel.setVisibility(View.INVISIBLE);
        updateListView(tableName);

        if (sizeOfArray(tableName) > 0) {
         for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
          FindedUser name = entry.getValue();
          if (IntNormalIdWthDot == name.getId()) {
           goToConversation(name.getId(), name.getName(), conversationName);
          }
         }
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(
          Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//       } else if (response.code() == 400) {
//        Toast.makeText(SearchActivity.this, response.message(), Toast.LENGTH_LONG).show();
//       }
      }

      @Override
      public void onFailure(@NonNull Call<FindedConverName> call, @NonNull Throwable t) {
       Toast.makeText(SearchActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
      }
     });
    }
   }
  });
  // go to conversation
  addedUsersListView.setOnItemClickListener((parent, view, position, id) -> {
   if (sizeOfArray(tableName) > 0) {
    int count = 0;
    for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
     FindedUser name = entry.getValue();
     if (id == count) {
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
    if (id == count) {
     chosenUser = entry.getValue().getName();
    }
    count++;
   }
   new android.app.AlertDialog.Builder(SearchActivity.this)
     .setIcon(android.R.drawable.ic_dialog_alert)
     .setTitle("Delete conversation?")
     .setMessage(Html.fromHtml("\t\t\t\t\t\t" + "With " + "<b>" + chosenUser + "</b>", Html.FROM_HTML_MODE_LEGACY))
     .setPositiveButton("Yes", (dialog, which) -> {
      Integer itemToDelete = 0;
      int count1 = 0;
      for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
       FindedUser data = entry.getValue();

       if (id == count1) {
        itemToDelete = entry.getKey();
        System.out.println("test " + data.getConversationName());
        deleteConversationFDb(data.getConversationName());
       }
       count1++;
      }
      boolean isItem = deleteFromDb(itemToDelete, tableName); //send timestamp
      if (isItem) {
       helperMap.remove(itemToDelete);
       updateListView(tableName);
       MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
       addedUsersListView.setAdapter(addedUsersAdapter);
       addedUsersAdapter.notifyDataSetChanged();
      }
     }).setNegativeButton("No", null)
     .show();
   return true;
  });
 }

 // adds the user to the top of the list
 private void fncAddIfUserExist(int intNormalIdWthDot, String tableName) {
  contactListView.setVisibility(View.GONE);
  searchText.setText("");
  addedUsersListView.setVisibility(View.VISIBLE);
  findedUsersLabel.setVisibility(View.INVISIBLE);

  MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
  addedUsersListView.setAdapter(addedUsersAdapter);
  addedUsersAdapter.notifyDataSetChanged();

  updateListView(tableName);
  if (sizeOfArray(tableName) > 0) {
   for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
    FindedUser name = entry.getValue();
    if (intNormalIdWthDot == name.getId()) {
     goToConversation(name.getId(), name.getName(), name.getConversationName());
    }
   }
  }
  InputMethodManager imm = (InputMethodManager) getSystemService(
    Activity.INPUT_METHOD_SERVICE);
  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 }

 @Override
 public void onBackPressed() {
  super.onBackPressed();
  Intent a = new Intent(Intent.ACTION_MAIN);
  a.addCategory(Intent.CATEGORY_HOME);
  a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  startActivity(a);
  finish();
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
    if (!verActualPass || !verPassOne || !verPassTwo) {
     Toast.makeText(SearchActivity.this, "Check your passwords.", Toast.LENGTH_SHORT).show();
    } else {
     if (!(strNewPass.equals(strNewPassRep) || (strNewPass.length() == 0) || (strNewPassRep.length() == 0))) {
      Toast.makeText(SearchActivity.this, "New passwords are different.", Toast.LENGTH_SHORT).show();
     } else {
      String token = _appPrefs.getToken();
      String email = _appPrefs.getEmail();

      HashMap<String, String> map = new HashMap<>();
      map.put("actualPass", actualPass.getText().toString());
      map.put("newPassword", newPass.getText().toString());
      map.put("email", email);
      String authToken = "Bearer " + token;

      client.getServie().executePasswordChange(authToken, map).enqueue(new Callback<Void>() {
       @Override
       public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
        if (response.code() == 200) {
         Toast.makeText(SearchActivity.this, response.message(), Toast.LENGTH_SHORT).show();
         logout();
        } else if (response.code() == 401) {
         Toast.makeText(SearchActivity.this, "Password was not changed", Toast.LENGTH_SHORT).show();
        }
       }

       @Override
       public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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

 public void logout() {
  _appPrefs.saveRemember(false);
  _appPrefs.saveIsToken(false);
  _appPrefs.saveName("");
  _appPrefs.saveToken("");
  _appPrefs.saveEmail("");
  Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
  startActivity(intent);
 }

 public void deleteTable(String tableName) {
        messengerDB.execSQL("DELETE FROM "+tableName+"");
//        messengerDB.execSQL("DROP TABLE "+tableName+"");
//  messengerDB.execSQL("DETACH DATABASE CommisionaireDB");
  System.out.println("Delete table");
 }

 public void deleteConversationFDb(String convName) {
  String convNameFP = convName.substring(0, 8);
  String convNameLastV = "conv"+convNameFP;
  messengerDB.execSQL("DROP TABLE '"+convNameLastV+"'");
 }

 public boolean deleteFromDb(Integer itemToDelete, String tableName) {
  for (Map.Entry<Integer, FindedUser> entry : helperMap.entrySet()) {
   if (itemToDelete.equals(entry.getKey())) {
    Integer userId = entry.getValue().getId();
    String jwt = _appPrefs.getToken();
    HashMap<String, String> deleteUserFromDb = new HashMap<>();
    deleteUserFromDb.put("email", _appPrefs.getEmail());
    deleteUserFromDb.put("userIdToDelete", String.valueOf(userId));
    String authToken = "Bearer " + jwt;
    client.getServie().executeDeleteUserFromContactList(authToken, deleteUserFromDb).enqueue(new Callback<Void>() {
     @Override
     public void onResponse(Call<Void> call, Response<Void> response) {
     }

     @Override
     public void onFailure(Call<Void> call, Throwable t) {
     }
    });

    String sql = "DELETE FROM " + tableName + " WHERE timestamp = " + itemToDelete;
    SQLiteStatement statement = messengerDB.compileStatement(sql);
    statement.execute();
    return true;
   }
  }
  return false;
 }

 public void updateListView(String tableName) {
  Cursor c = messengerDB.rawQuery("SELECT * FROM " + tableName + "", null);
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
   helperMap.put(entry.getKey(), new FindedUser(user.getId(), user.getName(), user.getConversationName(),
     user.getTimestamp()));
  }
  MyAdapterHash addedUsersAdapter = new MyAdapterHash(helperMap);
  addedUsersListView.setAdapter(addedUsersAdapter);
  addedUsersAdapter.notifyDataSetChanged();
 }

 public boolean isUserExistInDb(int userIdFromDb, String tableName) {
  System.out.println(userIdFromDb);
  if (addedUsersMap.size() == 0) {
   return false;
  } else {
   Cursor c = messengerDB.rawQuery("SELECT * FROM " + tableName + "", null);
   userIndex = c.getColumnIndex("userId");
   if (c.moveToFirst()) {
    do {
     int intUserId = c.getInt(userIndex);
     if (intUserId == userIdFromDb) {
      int tsLong = (int) (System.currentTimeMillis() / 1000);
      String sql = "UPDATE " + tableName + " SET timestamp = " + tsLong + " WHERE userId = " + userIdFromDb;
      SQLiteStatement statement = messengerDB.compileStatement(sql);
      statement.execute();
      return true;
     }
    } while (c.moveToNext());
   }
  }
  return false;
 }

 public int sizeOfArray(String tableName) {
  int aaa = 0;
  Cursor c = messengerDB.rawQuery("SELECT * FROM " + tableName + "", null);
  if (c.moveToFirst()) {
   do {
    aaa++;
   } while (c.moveToNext());
  }
  return aaa;
 }

 public void goToConversation(int userId, String recipient, String conversationName) {
  String myName = _appPrefs.getName();
  Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
  intent.putExtra("userId", userId);
  intent.putExtra("name", recipient);
  intent.putExtra("myName", myName);
  intent.putExtra("conversationName", conversationName);
  startActivity(intent);
 }

 public boolean isTableExists(String tableName) {
  Cursor c = null;
  boolean tableExists = false;
  try {
   c = messengerDB.query(tableName, null,
     null, null, null, null, null);
   tableExists = true;
  } catch (Exception e) {
   System.out.println("Table not exist");
  }

  return tableExists;
 }

 public void checkContacts(String tableName) {
  String jwt = _appPrefs.getToken();
  HashMap<String, String> mapCheckContact = new HashMap<>();
  mapCheckContact.put("email", _appPrefs.getEmail());
  String authToken = "Bearer " + jwt;
  client.getServie().executeCheckContacts(authToken, mapCheckContact).enqueue(new Callback<CheckContacts>() {
   @Override
   public void onResponse(@NonNull Call<CheckContacts> call, @NonNull Response<CheckContacts> response) {
//    ArrayList<Object> fug;
    CheckContacts checkContacts = response.body();
    assert checkContacts != null;
    System.out.println("contacts on server " + checkContacts.getFindedUsers());
//     fug = checkContacts.getFindedUsers();
//     System.out.println("fug "+fug);
//       if(checkContacts.getFindedUsers().size() == 0){
//           updateListView(tableName);
//       }else{
//           Toast.makeText(SearchActivity.this, response.message(), Toast.LENGTH_SHORT).show();
//       }
   }

   @Override
   public void onFailure(@NonNull Call<CheckContacts> call, @NonNull Throwable t) {}
  });
 }

 public void checkMessages() {
  String jwt = _appPrefs.getToken();
  HashMap<String, String> mapCheckMessages = new HashMap<>();
  mapCheckMessages.put("email", _appPrefs.getEmail());
  String authToken = "Bearer " + jwt;
  client.getServie().executeCheckMessages(authToken, mapCheckMessages).enqueue(new Callback<CheckMessages>() {
   @Override
   public void onResponse(@NonNull Call<CheckMessages> call, @NonNull Response<CheckMessages> response) {
    CheckMessages checkMessages = response.body();
    assert checkMessages != null;
    // adds a user if it did not exist before
    if(checkMessages.getOfflineMessages() != null){
     for (Object o : checkMessages.getOfflineMessages()) {
      Gson gson = new Gson();
      String json = gson.toJson(o);
      try{
       JSONObject objj = new JSONObject(json);
       String checkUserId = String.valueOf(objj.get("id"));
       String name = (String) objj.get("name");
       String convName = (String) objj.get("conversationName");
       int indexForId = checkUserId.indexOf('.');
       String userIdFromDb = checkUserId.substring(0, indexForId);
       String StrNormalIdWthDot = userIdFromDb.substring(0, indexForId);
       int intNormalIdWthDot = Integer.parseInt(StrNormalIdWthDot);

       for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
        FindedUser usser = entry.getValue();
        if (intNormalIdWthDot != usser.getId()) {
         System.out.println("Dodaj usera");
         saveToLikedHashMap(StrNormalIdWthDot, name, convName);
        }
       }

//      Iterator it = addedUsersMap.entrySet().iterator();
//      while (it.hasNext()) {
//       Map.Entry pair = (Map.Entry)it.next();
//       FindedUser usser = (FindedUser) pair.getValue();
//       System.out.println(pair.getKey() + " = " + pair.getValue()+ " = "+usser.getId());
//       it.remove();
//      }
      }catch(JSONException e){
       e.printStackTrace();
      }

     }

     System.out.println("offline messages " + checkMessages.getOfflineMessages());
     for (Object o : checkMessages.getOfflineMessages()) {
      System.out.println(o);
      Gson gson = new Gson();
      String json = gson.toJson(o);
      try {
       JSONObject obj = new JSONObject(json);
       String userId = String.valueOf(obj.get("id"));
       String name = (String) obj.get("name");
       String message = (String) obj.get("message");
       String convName = (String) obj.get("conversationName");
       String createdAt = (String) obj.get("createdAt");
       String convNameFP = convName.substring(0, 8);
       String convNameLastV = "conv"+convNameFP;
       if (isTableExists(convNameLastV)) {
//       System.out.println("Istnieje");
        String sql = "INSERT INTO '"+convNameLastV+"' (name, message, isSent, timestamp) VALUES (?,?,?,?)";
        SQLiteStatement statement = messengerDB.compileStatement(sql);
        statement.bindString(1, name);
        statement.bindString(2, message);
        statement.bindString(3, String.valueOf(false));
        statement.bindString(4, createdAt);
        statement.execute();

        //      String userIdFromDbb = userId;
//       int indexForId = userId.indexOf('.');
//       String userIdFromDb = userId.substring(0, indexForId);
//
//       String StrNormalIdWthDot = userIdFromDb.substring(0, indexForId);
//       int IntNormalIdWthDot = Integer.parseInt(StrNormalIdWthDot);
//      System.out.println(isUserExistInDb(IntNormalIdWthDot, String tableName));
//       for (Map.Entry<Integer, FindedUser> entry : addedUsersMap.entrySet()) {
//        FindedUser usser = entry.getValue();
//        if (IntNormalIdWthDot != usser.getId()) {
////        goToConversation(name.getId(), name.getName(), conversationName);
//         System.out.println("Dodaj usera");
//        }
//       }
       } else {
        System.out.println("Brak " + convNameLastV);
        messengerDB.execSQL("CREATE TABLE IF NOT EXISTS '"+convNameLastV+"' (id INTEGER PRIMARY KEY, name VARCHAR, message VARCHAR, isSent BOOLEAN, timestamp INTEGER)");

        String sql = "INSERT INTO '"+convNameLastV+"' (name VARCHAR, message VARCHAR, isSent BOOLEAN, timestamp INTEGER) VALUES (?,?,?,?)";
        SQLiteStatement statement = messengerDB.compileStatement(sql);
        statement.bindString(1, name);
        statement.bindString(2, message);
        statement.bindString(3, String.valueOf(false));
        statement.bindString(4, createdAt);
        statement.execute();

        //      String userIdFromDbb = userId;

//      System.out.println(isUserExistInDb(IntNormalIdWthDot, String tableName));

       }

      } catch (JSONException e) {
       e.printStackTrace();
      }
     }
    }else{
     System.out.println(" We have a null");
    }
   }

   @Override
   public void onFailure(@NonNull Call<CheckMessages> call, @NonNull Throwable t) {
    System.out.println(t.getMessage());
   }
  });
 }
 private void saveToLikedHashMap(String strNormalIdWthDot, String name, String convName){
  int tsLong = (int) (System.currentTimeMillis() / 1000);
//        FindedConverName result = new FindedConverName();
//        conversationName = result.getConversationName();
//        fncAddIfUserNotExist(tsLong, StrNormalIdWthDot, name, intNormalIdWthDot);
  String sql = "INSERT INTO " + tableName + " (userId, name, conversationName, timestamp) VALUES (?,?,?,?)";
  SQLiteStatement statement = messengerDB.compileStatement(sql);
  statement.bindString(1, strNormalIdWthDot);
  statement.bindString(2, name);
  statement.bindString(3, convName);
  statement.bindString(4, String.valueOf(tsLong));
  statement.execute();

  updateListView(tableName);
 }
}