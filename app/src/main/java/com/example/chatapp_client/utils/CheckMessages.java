package com.example.chatapp_client.utils;

import java.util.ArrayList;

public class CheckMessages {
  public ArrayList<Object> getOfflineMessages() {
    return offlineMessages;
  }

  public void setOfflineMessages(ArrayList<Object> offlineMessages) {
    this.offlineMessages = offlineMessages;
  }

  private ArrayList<Object> offlineMessages;
}
