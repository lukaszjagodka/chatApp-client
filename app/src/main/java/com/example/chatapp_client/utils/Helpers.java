package com.example.chatapp_client.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {
  private Context context;
  public Helpers(Context context) {
    this.context = context;
  }

  public void closeKeyboard(Activity activity) {
    InputMethodManager imm = (InputMethodManager) context
        .getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }
  public static boolean validationPassword(EditText passwordEdit, Context context) {
    if(passwordEdit.length() != 0){
      if (passwordEdit.getText().toString().length() < 5 && !isValidPassword(passwordEdit.getText().toString())) {
        Toast.makeText(context,"Password incorrect", Toast.LENGTH_LONG).show();
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
  public static boolean validationEmailAddress(EditText email, Context context){
    String emailInput = email.getText().toString();
    if(!emailInput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
      return true;
    }else{
      return false;
    }
  }
}
