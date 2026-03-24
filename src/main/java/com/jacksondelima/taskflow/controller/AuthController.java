package com.jacksondelima.taskflow.controller;

import com.jacksondelima.taskflow.dto.auth.AuthResponseDTO;
import com.jacksondelima.taskflow.dto.auth.LoginRequestDTO;
import com.jacksondelima.taskflow.dto.auth.RegisterRequestDTO;
import com.jacksondelima.taskflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok("Usuário criado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}