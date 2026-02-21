package com.ntews.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            // Simple authentication for MVP - in production, use proper JWT
            if ("admin".equals(request.getUsername()) && "admin123".equals(request.getPassword())) {
                String token = UUID.randomUUID().toString();
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("token", token);
                response.put("user", Map.of(
                    "id", "1",
                    "username", request.getUsername(),
                    "role", "admin",
                    "permissions", new String[]{"read", "write", "delete"}
                ));
                response.put("expiresIn", 3600);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(Map.of(
                    "status", "error",
                    "message", "Invalid credentials"
                ));
            }
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Authentication failed"
            ));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        // Simple token validation for MVP
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("user", Map.of(
            "id", "1",
            "username", "admin",
            "role", "admin"
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Authentication Service");
        status.put("status", "running");
        status.put("timestamp", java.time.LocalDateTime.now());
        status.put("authType", "simple_token_based");
        
        return ResponseEntity.ok(status);
    }
    
    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
