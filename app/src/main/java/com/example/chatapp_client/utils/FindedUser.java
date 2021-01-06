package com.example.chatapp_client.utils;

public class FindedUser {
  private Integer id;
  private String name;
  private Integer timestamp;
  private String conversationName;

  public FindedUser(int id, String name, String conversationName, int timestamp) {
    this.id = id;
    this.name = name;
    this.timestamp = timestamp;
    this.conversationName = conversationName;
  }
  public String getConversationName() {
    return conversationName;
  }

  public void setConversationName(String conversationName) {
    this.conversationName = conversationName;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Integer timestamp) {
    this.timestamp = timestamp;
  }
  @Override
  public String toString() {
    return "FindedUser{" +
        "name='" + name + '\'' +
        ", id='" + id + '\'' +
        ", timestamp='" + timestamp + '\'' +
        '}';
  }
}
