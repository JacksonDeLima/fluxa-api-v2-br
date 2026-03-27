package com.jacksondelima.fluxa.seguranca;

import com.jacksondelima.fluxa.excecao.RecursoNaoEncontradoException;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public Usuario buscar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Usuario nao autenticado.");
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado."));
    }
}
