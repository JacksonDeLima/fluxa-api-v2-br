package com.jacksondelima.fluxa.autenticacao;

import com.jacksondelima.fluxa.autenticacao.dto.AutenticacaoResponseDTO;
import com.jacksondelima.fluxa.autenticacao.dto.CadastroRequestDTO;
import com.jacksondelima.fluxa.autenticacao.dto.LoginRequestDTO;
import com.jacksondelima.fluxa.excecao.RegraDeNegocioException;
import com.jacksondelima.fluxa.seguranca.JwtService;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void cadastrar(CadastroRequestDTO request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraDeNegocioException("E-mail ja cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.USUARIO)
                .build();

        usuarioRepository.save(usuario);
    }

    public AutenticacaoResponseDTO login(LoginRequestDTO request) {
        String email = normalizarEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.senha()
                )
        );

        String token = jwtService.generateToken(email);
        return new AutenticacaoResponseDTO(token, "Bearer");
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
