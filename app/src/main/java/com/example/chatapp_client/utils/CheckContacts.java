package com.example.chatapp_client.utils;

import java.util.ArrayList;

public class CheckContacts {
  public ArrayList<Object> getFindedUsers() {
    return findedUsers;
  }

  public void setFindedUsers(ArrayList<Object> findedUsers) {
    this.findedUsers = findedUsers;
  }

  private ArrayList<Object> findedUsers;
}
