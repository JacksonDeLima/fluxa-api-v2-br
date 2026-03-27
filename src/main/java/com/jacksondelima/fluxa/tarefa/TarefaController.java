package com.jacksondelima.fluxa.tarefa;

import com.jacksondelima.fluxa.tarefa.dto.TarefaRequestDTO;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping
    @Operation(summary = "Criar nova tarefa")
    public ResponseEntity<TarefaResponseDTO> criar(@RequestBody @Valid TarefaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaService.criar(request));
    }

    @GetMapping
    @Operation(summary = "Listar tarefas do usuario autenticado")
    public ResponseEntity<List<TarefaResponseDTO>> listarMinhas() {
        return ResponseEntity.ok(tarefaService.listarMinhas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por id")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa")
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid TarefaRequestDTO request
    ) {
        return ResponseEntity.ok(tarefaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tarefa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
