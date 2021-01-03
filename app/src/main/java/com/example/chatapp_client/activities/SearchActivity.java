package com.example.chatapp_client.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.retrofit.RetrofitClient;
import com.example.chatapp_client.utils.Helpers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    public AppPreferences _appPrefs;
    RetrofitClient client = new RetrofitClient();

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
                        System.out.println(actualPass.getText().toString() + " "+newPass.getText().toString()+ " "+email+" "+authToken);

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
}