package com.bluewave.controller;

import com.bluewave.dto.*;
import com.bluewave.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/check-available")
    public ResponseEntity<Map<String, Boolean>> checkAvailable(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(authService.checkAvailable(email, username));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequestDTO dto) {
        String message = authService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verify(@Valid @RequestBody OtpVerfiyRequestDTO dto) {
        return ResponseEntity.ok(authService.verify(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody AuthNewTokenRequestDTO dto) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody AuthNewTokenRequestDTO dto) {
        String message = authService.logout(dto);
        return ResponseEntity.ok(Map.of("message", message));
    }


}
