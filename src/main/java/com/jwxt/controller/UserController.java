package com.jwxt.controller;

import com.jwxt.common.Result;
import com.jwxt.entity.User;
import com.jwxt.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        return Result.success(userService.listUsers());
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestBody Map<String, Boolean> body) {
        userService.updateUserStatus(userId, body.get("enabled"));
        return Result.success();
    }
}
