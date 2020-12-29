package com.example.chatapp_client.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.chatapp_client.R;
import com.example.chatapp_client.retrofit.RetrofitInterface;
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
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Objects.requireNonNull(getSupportActionBar()).hide();

        String BASE_URL = "http://192.168.100.3:3001";
        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        findViewById(R.id.signup).setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.signup_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
            builder.setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPause(dialog);
                    }
                });
            if (!isFinishing()) {
                builder.setView(view).show();
            }
            Button signupBtn = view.findViewById(R.id.signup);
            final EditText nameEdit = view.findViewById(R.id.nameEdit);
            final EditText emailEdit = view.findViewById(R.id.emailEdit);
            final EditText passwordEdit = view.findViewById(R.id.passwordEdit);
            signupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
//                                        finish();
//                                        startActivity(getIntent());
                                } else if (response.code() == 400) {
                                    Toast.makeText(SignupActivity.this,
                                        "User already exists.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(SignupActivity.this, t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        });


        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.login_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setNegativeButton(getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                builder.setView(view).show();
            }
        });
        if (isFinishing()) {
            onPause();
        }
    }
    public void onPause(DialogInterface dialog){
        runOnUiThread(dialog::dismiss);
        System.out.println("dziala");
    }
    public boolean validationEmailAddress(EditText email){
        String emailInput = email.getText().toString();
        if(!emailInput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            return true;
        }else{
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