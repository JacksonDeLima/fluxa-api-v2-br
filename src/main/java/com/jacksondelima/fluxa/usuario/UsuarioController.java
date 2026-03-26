package com.jacksondelima.fluxa.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    @GetMapping("/eu")
    @Operation(summary = "Obter usuario autenticado")
    public ResponseEntity<String> obterUsuarioAutenticado() {
        return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
