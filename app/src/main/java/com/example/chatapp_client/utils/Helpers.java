package com.example.chatapp_client.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

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
}
