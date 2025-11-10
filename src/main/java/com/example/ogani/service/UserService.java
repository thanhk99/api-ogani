package com.example.ogani.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.dtos.request.UpdateProfileRequest;
import com.example.ogani.models.User;
import com.example.ogani.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> updateUser(UpdateProfileRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        User userByemail = userRepository.findByEmail(request.getEmail());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "bad request", "data", "username is not valid"));
        }
        if (userByemail != null && !userByemail.getEmail().equals(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("msg", "bad request", "data", "email is already use 1"));
        }
        User newUser = user.get();
        newUser.setAddress(request.getAddress());
        newUser.setCountry(request.getCountry());
        newUser.setPhone(request.getPhone());
        newUser.setFirstname(request.getFirstname());
        newUser.setLastname(request.getLastname());
        newUser.setEmail(request.getEmail());
        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("msg", "success", "data", "save successfull"));
    }

    public ResponseEntity<?> getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("msg", "bad request", "data", "username is not valid"));
        }
        return ResponseEntity.ok(user);
    }

}
