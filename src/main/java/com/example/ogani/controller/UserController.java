package com.example.ogani.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.request.ChangePasswordRequest;
import com.example.ogani.dtos.request.UpdateProfileRequest;
import com.example.ogani.models.User;
import com.example.ogani.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> getuser(@RequestParam("username") String username) {
        return userService.getUserByUsername(username);

    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        return userService.updateUser(request);
    }

    // @PutMapping("/password")
    // public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest
    // request){
    // userService.changePassword(request);
    // return ResponseEntity.ok(new MessageResponse("Change Password Success!"));
    // }
}
