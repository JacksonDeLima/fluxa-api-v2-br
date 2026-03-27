package com.jacksondelima.fluxa.usuario;

import com.jacksondelima.fluxa.seguranca.UsuarioAutenticadoService;
import com.jacksondelima.fluxa.usuario.dto.UsuarioAutenticadoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @GetMapping("/eu")
    @Operation(summary = "Obter usuario autenticado")
    public ResponseEntity<UsuarioAutenticadoResponseDTO> obterUsuarioAutenticado() {
        return ResponseEntity.ok(UsuarioAutenticadoResponseDTO.from(usuarioAutenticadoService.buscar()));
    }
}
