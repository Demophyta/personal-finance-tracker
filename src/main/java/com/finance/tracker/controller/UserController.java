package com.finance.tracker.controller;

import com.finance.tracker.dto.UpdateUserRequestDTO;
import com.finance.tracker.dto.UserResponseDTO;
import com.finance.tracker.model.User;
import com.finance.tracker.service.UserService;
import com.finance.tracker.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getProfile(@CurrentUser User user) {
        return ResponseEntity.ok(userService.getUserProfile(user));
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateProfile(@CurrentUser User user,
                                                         @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUserProfile(user, dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount(@CurrentUser User user) {
        userService.deleteUser(user);
        return ResponseEntity.ok("User account deleted successfully.");
    }
}
