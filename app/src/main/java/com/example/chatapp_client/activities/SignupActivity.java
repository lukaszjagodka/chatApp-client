package com.example.chatapp_client.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.retrofit.RetrofitClient;
import com.example.chatapp_client.utils.Helpers;
import com.example.chatapp_client.utils.LoginResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private AppPreferences _appPrefs;
    Context context = SignupActivity.this;
    Helpers newH = new Helpers(context);
    RetrofitClient client = new RetrofitClient();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Objects.requireNonNull(getSupportActionBar()).hide();

        _appPrefs = new AppPreferences(getApplicationContext());

        Boolean isToken = _appPrefs.getIsToken();
        Boolean remember = _appPrefs.getRemember();
        if(remember.equals(true)) {
            if (isToken.equals(true)) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        }else if(remember.equals(false)){
            _appPrefs.saveRemember(false);
            _appPrefs.saveIsToken(false);
            _appPrefs.saveToken("");
            _appPrefs.saveEmail("");
            _appPrefs.saveName("");
        }

        findViewById(R.id.login).setOnClickListener(view -> handleLoginDialog());
        findViewById(R.id.signup).setOnClickListener(view -> handleSignupDialog());
    }
    private void handleSignupDialog(){
        View view = getLayoutInflater().inflate(R.layout.signup_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setNegativeButton(getString(android.R.string.cancel),
            (dialog, which) -> dialog.dismiss());
        if (!isFinishing()) {
            builder.setView(view).show();
        }
        Button signupBtn = view.findViewById(R.id.signup);
        final EditText nameEdit = view.findViewById(R.id.nameEdit);
        final EditText emailEdit = view.findViewById(R.id.emailEdit);
        final EditText passwordEdit = view.findViewById(R.id.passwordEdit);
        signupBtn.setOnClickListener(v1 -> {
            Boolean validEmailAddress = Helpers.validationEmailAddress(emailEdit, SignupActivity.this);
            Boolean validPassword = Helpers.validationPassword(emailEdit, SignupActivity.this);
            if(validEmailAddress && validPassword){
                HashMap<String, String> map = new HashMap<>();
                map.put("name", nameEdit.getText().toString());
                map.put("email", emailEdit.getText().toString());
                map.put("password", passwordEdit.getText().toString());
                client.getServie().executeSignup(map).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.code() == 200) {
                            Toast.makeText(SignupActivity.this,
                                "Signed up successfully. Check your email and confirm account!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                            startActivity(intent);
                        } else if (response.code() == 400) {
                            _appPrefs.saveIsToken(false);
                            _appPrefs.saveRemember(false);
                            Toast.makeText(SignupActivity.this,
                                "User already exists.", Toast.LENGTH_LONG).show();
                        }
                        newH.closeKeyboard(SignupActivity.this);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(SignupActivity.this, t.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    private void handleLoginDialog() {
        View view = getLayoutInflater().inflate(R.layout.login_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(getString(android.R.string.cancel),
            (dialog, which) -> dialog.dismiss());
        builder.setView(view).show();

        Button loginBtn = view.findViewById(R.id.login);
        Button remindPassBtn = view.findViewById(R.id.remindPassBtn);
        final EditText emailEdit = view.findViewById(R.id.emailEditLgn);
        final EditText passwordEdit = view.findViewById(R.id.passwordEditLgn);
        final CheckBox rememberBox = view.findViewById(R.id.checkBoxRem);
        TextView remindPass = view.findViewById(R.id.remindPass);

        loginBtn.setOnClickListener(v -> {
            if(emailEdit.getText().toString().trim().length() > 0){
                if(passwordEdit.getText().toString().trim().length() > 0){
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", emailEdit.getText().toString());
                    map.put("password", passwordEdit.getText().toString());

                    client.getServie().executeLogin(map).enqueue(new Callback<LoginResult>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginResult> call, @NonNull Response<LoginResult> response) {
                            if (response.code() == 200) {
                                LoginResult result = response.body();
                                assert result != null;
                                String token = result.getJwtToken();
                                result.setToken(token);
                                JWT jwt = new JWT(token);
                                Claim name = jwt.getClaim("name");
                                result.setName(name.asString());
                                Claim email = jwt.getClaim("email");
                                result.setEmail(email.asString());
                                result.setLoged(true);

                                _appPrefs.saveToken(String.valueOf(jwt));
                                _appPrefs.saveIsToken(true);
                                _appPrefs.saveName(name.asString());
                                _appPrefs.saveEmail(result.getEmail());
                                _appPrefs.saveRemember(rememberBox.isChecked());

                                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(intent);
                            } else if (response.code() == 404) {
                                Toast.makeText(SignupActivity.this, "Wrong credentials",
                                    Toast.LENGTH_LONG).show();
                                newH.closeKeyboard(SignupActivity.this);

                                remindPass.setText("Remind password");
                                remindPass.setOnClickListener(v1 -> {
                                    rememberBox.setVisibility(View.GONE);
                                    loginBtn.setVisibility(View.GONE);
                                    remindPass.setVisibility(View.GONE);
                                    passwordEdit.setVisibility(View.GONE);
                                    remindPassBtn.setVisibility(View.VISIBLE);
                                    remindPassBtn.setOnClickListener(v11 -> {
                                        HashMap<String, String> map1 = new HashMap<>();
                                        map1.put("email", emailEdit.getText().toString());
                                        client.getServie().executeRememberPassword(map1).enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                                if (response.isSuccessful()) {
                                                    if (response.code() == 200) {
                                                        Toast.makeText(SignupActivity.this,
                                                            "If email was correct password was sent on email.", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                                                        startActivity(intent);
                                                        newH.closeKeyboard(SignupActivity.this);
                                                    } else if (response.code() == 400) {
                                                        Toast.makeText(SignupActivity.this,
                                                            "Wrong email", Toast.LENGTH_LONG).show();
                                                    }
                                                    newH.closeKeyboard(SignupActivity.this);
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                                Toast.makeText(SignupActivity.this, t.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    });
                                });
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<LoginResult> call, @NonNull Throwable t) {
                            Toast.makeText(SignupActivity.this, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    newH.closeKeyboard(SignupActivity.this);
                    Toast.makeText(SignupActivity.this, "Enter the password", Toast.LENGTH_SHORT).show();
                }
            }else{
                newH.closeKeyboard(SignupActivity.this);
                Toast.makeText(SignupActivity.this, "Enter the email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}