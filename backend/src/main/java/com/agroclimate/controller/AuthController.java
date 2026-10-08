package com.agroclimate.controller;

import com.agroclimate.dto.auth.AuthResponse;
import com.agroclimate.dto.auth.CurrentUserDto;
import com.agroclimate.dto.auth.LoginRequest;
import com.agroclimate.dto.auth.RegisterRequest;
import com.agroclimate.security.CustomUserDetails;
import com.agroclimate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public CurrentUserDto me(Authentication authentication) {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        return authService.currentUser(details.getUser());
    }
}
