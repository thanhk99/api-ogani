package com.example.ogani.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.ogani.dtos.request.UpdateProfileRequest;
import com.example.ogani.models.User;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.security.jwt.JwtUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> updateUser(UpdateProfileRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        User userByemail = userRepository.findByEmail(request.getEmail());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "bad request", "data", "username is not valid"));
        }
        if (userByemail != null) {
            return ResponseEntity.badRequest().body(Map.of("msg", "bad request", "data", "email is already use"));
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

    public ResponseEntity<?> getUserByUsername(String header) {
        String jwt = header.substring(7);
        Long id = jwtUtil.getUserIdFromToken(jwt);
        User user = userRepository.findByUid(id);
        if(user == null){
            Re
        }
    }

}
