package com.jacksondelima.fluxa.administracao;

import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/administracao")
@RequiredArgsConstructor
@Tag(name = "Administracao")
public class AdministracaoController {

    private final AdministracaoService administracaoService;

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios do sistema")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarUsuarios(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) Perfil perfil,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(administracaoService.listarUsuarios(termo, perfil, pageable));
    }

    @GetMapping("/tarefas")
    @Operation(summary = "Listar tarefas do sistema")
    public ResponseEntity<Page<TarefaResponseDTO>> listarTarefas(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) String emailUsuario,
            @PageableDefault(size = 20, sort = "criadaEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(administracaoService.listarTarefas(termo, status, emailUsuario, pageable));
    }
}
