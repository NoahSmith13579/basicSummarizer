package com.example.summarizer.controller;

import com.example.summarizer.dto.request.LoginRequest;
import com.example.summarizer.dto.response.TokenResponse;
import com.example.summarizer.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final JwtUtil jwtUtil;

  public AuthController(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }


  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {

    // Replace with real user validation
    if (req.username().equals("testuser") && req.password().equals("password")) {
      String token = jwtUtil.generateToken(req.username());
      return ResponseEntity.ok(new TokenResponse(token));
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}

