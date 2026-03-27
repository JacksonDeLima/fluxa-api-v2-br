package com.jacksondelima.fluxa.usuario.dto;

import com.jacksondelima.fluxa.usuario.Usuario;

public record UsuarioAutenticadoResponseDTO(
        Long id,
        String nome,
        String email,
        String perfil
) {

    public static UsuarioAutenticadoResponseDTO from(Usuario usuario) {
        return new UsuarioAutenticadoResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name()
        );
    }
}
