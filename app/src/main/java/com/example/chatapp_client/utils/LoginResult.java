package com.example.chatapp_client.utils;

public class LoginResult {
  private Integer id;
  private String name;
  private String email;
  private String token;
  private Boolean isLoged = false;
  private String jwtToken;

  public Integer getId() { return id; }
  public void setId(Integer id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }
  public Boolean getLoged() { return isLoged; }
  public void setLoged(Boolean loged) { isLoged = loged; }
  public String getJwtToken() { return jwtToken; }
  public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }
}
