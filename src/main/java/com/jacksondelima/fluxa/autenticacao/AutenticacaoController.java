package com.jacksondelima.fluxa.autenticacao;

import com.jacksondelima.fluxa.autenticacao.dto.AutenticacaoResponseDTO;
import com.jacksondelima.fluxa.autenticacao.dto.CadastroRequestDTO;
import com.jacksondelima.fluxa.autenticacao.dto.LoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autenticacao")
@RequiredArgsConstructor
@Tag(name = "Autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastrar usuario")
    public ResponseEntity<String> cadastrar(@RequestBody @Valid CadastroRequestDTO request) {
        autenticacaoService.cadastrar(request);
        return ResponseEntity.ok("Usuario cadastrado com sucesso.");
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login")
    public ResponseEntity<AutenticacaoResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        return ResponseEntity.ok(autenticacaoService.login(request));
    }
}
