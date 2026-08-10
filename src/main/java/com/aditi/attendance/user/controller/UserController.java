package com.aditi.attendance.user.controller;

import com.aditi.attendance.user.dto.LoginRequest;
import com.aditi.attendance.user.dto.SetupPasswordRequest;
import com.aditi.attendance.user.dto.SignupRequest;
import com.aditi.attendance.user.dto.UserRequest;
import com.aditi.attendance.user.dto.UserResponse;
import com.aditi.attendance.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {

        return userService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UserRequest request) {

        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);
    }

    @GetMapping("/search")
    public List<UserResponse> searchUser(@RequestParam String keyword) {

        return userService.searchUser(keyword);
    }

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {

        return userService.signupAdmin(request);
    }

    @PostMapping("/employee/setup-password")
    public UserResponse setupEmployeePassword(@Valid @RequestBody SetupPasswordRequest request) {

        return userService.setupEmployeePassword(request);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {

        return userService.login(request);
    }

}