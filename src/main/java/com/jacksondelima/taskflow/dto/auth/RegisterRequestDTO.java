package com.jacksondelima.taskflow.dto.auth;

public record RegisterRequestDTO(
        String name,
        String email,
        String password
) {}