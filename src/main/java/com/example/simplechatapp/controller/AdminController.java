package com.example.simplechatapp.controller;


import com.example.simplechatapp.dto.UserDTO;
import com.example.simplechatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final UserService userService;


    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> activeUsers = userService.getAllUsers();
        return ResponseEntity.ok(activeUsers);
    }


    @PutMapping("/users/{userId}/delete")
    public ResponseEntity<Void> softDelete(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/restore")
    public ResponseEntity<UserDTO> restoreUser(@PathVariable Long userId) {
        UserDTO restoredUser = userService.restoreUser(userId);
        return ResponseEntity.ok(restoredUser);
    }

}
