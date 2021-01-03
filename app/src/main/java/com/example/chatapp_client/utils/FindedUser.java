package com.example.chatapp_client.utils;

public class FindedUser {
  private Integer id;
  private String name;
  private Integer timestamp;

  public FindedUser(int id, String name, int timestamp) {
    this.id = id;
    this.name = name;
    this.timestamp = timestamp;
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
