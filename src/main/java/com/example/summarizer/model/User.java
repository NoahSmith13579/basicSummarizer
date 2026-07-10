package com.example.summarizer.model;

//import jakarta.persistence.*;

import java.time.LocalDateTime;

// TODO: Do I need to manually implement password hashing?

//@Entity
//@Table(name= "\"user\"")
public class User {
  //@Id
  //@GeneratedValue(strategy = GenerationType.IDENTITY)
  //@Column(name= "user_id")
  private Long id;
  //@Column(name = "username")
  private String username;
  //@Column(name = "email")
  private String email;
  //@Column(name = "password")
  private String password;
  //@Column(name = "created_at")
  private LocalDateTime createdAt;

  public User() {
  }

  public User(Long id, String username, String email, String password) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
