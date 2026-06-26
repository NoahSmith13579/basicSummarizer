package com.example.summarizer.interfaces;

import com.example.summarizer.model.User;

public interface UserService {
    User getUserByUsername(String username);
}
