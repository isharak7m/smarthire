package com.smarthire.controller;

import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import com.smarthire.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller — demonstrates JWT login + registration.
 * POST /api/auth/login   → returns JWT token
 * POST /api/auth/register → creates account, returns JWT
 * GET  /api/auth/me      → returns current user info (requires token)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── POST /api/auth/login ──────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required"));
        }

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    // ── POST /api/auth/register ───────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email    = request.get("email");
        String password = request.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "username, email, and password are required"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "token", token,
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ));
    }

    // ── GET /api/auth/me ──────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }
}
