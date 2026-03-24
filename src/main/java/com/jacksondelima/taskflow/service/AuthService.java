package com.jacksondelima.taskflow.service;

import com.jacksondelima.taskflow.dto.auth.AuthResponseDTO;
import com.jacksondelima.taskflow.dto.auth.LoginRequestDTO;
import com.jacksondelima.taskflow.dto.auth.RegisterRequestDTO;
import com.jacksondelima.taskflow.entity.User;
import com.jacksondelima.taskflow.enums.Role;
import com.jacksondelima.taskflow.repository.UserRepository;
import com.jacksondelima.taskflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.email());
        return new AuthResponseDTO(token);
    }
}