package com.jacksondelima.fluxa.usuario.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String perfil,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
