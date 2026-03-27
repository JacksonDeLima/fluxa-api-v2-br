package com.jacksondelima.fluxa.usuario.dto;

import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }
}
