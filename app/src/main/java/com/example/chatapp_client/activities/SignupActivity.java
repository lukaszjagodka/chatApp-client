package com.example.chatapp_client.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.example.chatapp_client.R;
import com.example.chatapp_client.appPreferences.AppPreferences;
import com.example.chatapp_client.retrofit.RetrofitInterface;
import com.example.chatapp_client.utils.LoginResult;
import com.example.chatapp_client.utils.Helpers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private RetrofitInterface retrofitInterface;
    private AppPreferences _appPrefs;
    Context context = SignupActivity.this;
    Helpers newH = new Helpers(context);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Objects.requireNonNull(getSupportActionBar()).hide();

        _appPrefs = new AppPreferences(getApplicationContext());

        String BASE_URL = "http://192.168.100.3:3001";
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

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
            Boolean validEmailAddress = validationEmailAddress(emailEdit);
            Boolean validPassword = validationPassword(emailEdit);
            if(validEmailAddress && validPassword){
                HashMap<String, String> map = new HashMap<>();
                map.put("name", nameEdit.getText().toString());
                map.put("email", emailEdit.getText().toString());
                map.put("password", passwordEdit.getText().toString());
                Call<Void> call = retrofitInterface.executeSignup(map);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
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
                    public void onFailure(Call<Void> call, Throwable t) {
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
//        String strEmail = emailEdit.getText().toString();
//        Boolean velStrEmail = validationEmailAddress(emailEdit);
        final EditText passwordEdit = view.findViewById(R.id.passwordEditLgn);
//        String strPasswordEdit = passwordEdit.getText().toString();
//        Boolean valStrPass = validationPassword(passwordEdit);
        final CheckBox rememberBox = view.findViewById(R.id.checkBoxRem);
        TextView remindPass = view.findViewById(R.id.remindPass);

        loginBtn.setOnClickListener(v -> {
            if(emailEdit.getText().toString().trim().length() > 0){
                if(passwordEdit.getText().toString().trim().length() > 0){
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", emailEdit.getText().toString());
                    map.put("password", passwordEdit.getText().toString());

                    Call<LoginResult> call = retrofitInterface.executeLogin(map);
                    call.enqueue(new Callback<LoginResult>() {
                        @Override
                        public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                            if (response.code() == 200) {
                                LoginResult result = response.body();
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
                                _appPrefs.saveName(result.getName());
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
                                        Call<Void> call1 = retrofitInterface.executeRememberPassword(map1);
                                        call1.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call1, Response<Void> response1) {
                                                if (response1.code() == 200) {
                                                    Toast.makeText(SignupActivity.this,
                                                        "If email was correct password was sent on email.", Toast.LENGTH_LONG).show();
                                                    newH.closeKeyboard(SignupActivity.this);
                                                } else if (response1.code() == 400) {
                                                    Toast.makeText(SignupActivity.this,
                                                        "Wrong email", Toast.LENGTH_LONG).show();
                                                }
//                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                                        startActivity(intent);
                                                newH.closeKeyboard(SignupActivity.this);
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call1, Throwable t) {
                                                Toast.makeText(SignupActivity.this, t.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    });
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResult> call, Throwable t) {
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

    public boolean validationEmailAddress(EditText email){
        String emailInput = email.getText().toString();
        if(!emailInput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            return true;
        }else{
//            Toast.makeText(this, "Make sure email address is correct", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public boolean validationPassword(EditText passwordEdit) {
        if(passwordEdit.length() != 0){
            if (passwordEdit.getText().toString().length() < 7 && !isValidPassword(passwordEdit.getText().toString())) {
                Toast.makeText(this,"Password incorrect", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        }else {
            return false;
        }
    }
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

}