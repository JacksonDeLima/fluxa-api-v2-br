package com.jacksondelima.fluxa.autenticacao;

import com.jacksondelima.fluxa.autenticacao.dto.AutenticacaoResponseDTO;
import com.jacksondelima.fluxa.autenticacao.dto.CadastroRequestDTO;
import com.jacksondelima.fluxa.autenticacao.dto.LoginRequestDTO;
import com.jacksondelima.fluxa.seguranca.JwtService;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void cadastrar(CadastroRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("E-mail ja cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.USUARIO)
                .build();

        usuarioRepository.save(usuario);
    }

    public AutenticacaoResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        String token = jwtService.generateToken(request.email());
        return new AutenticacaoResponseDTO(token);
    }
}
