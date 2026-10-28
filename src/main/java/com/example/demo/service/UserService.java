package com.example.demo.service;

import com.example.demo.model.User;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> login(String email, String password);
    Optional<User> getUserById(Long id);
    java.util.List<User> getAllUsers();
    void deleteUser(Long id);
}
