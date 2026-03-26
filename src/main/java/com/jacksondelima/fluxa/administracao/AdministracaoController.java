package com.jacksondelima.fluxa.administracao;

import com.jacksondelima.fluxa.tarefa.Tarefa;
import com.jacksondelima.fluxa.tarefa.TarefaRepository;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import com.jacksondelima.fluxa.usuario.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/administracao")
@RequiredArgsConstructor
@Tag(name = "Administracao")
public class AdministracaoController {

    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios do sistema")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(
                usuarioRepository.findAll()
                        .stream()
                        .map(this::mapearUsuario)
                        .toList()
        );
    }

    @GetMapping("/tarefas")
    @Operation(summary = "Listar tarefas do sistema")
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefas() {
        return ResponseEntity.ok(
                tarefaRepository.findAll()
                        .stream()
                        .map(this::mapearTarefa)
                        .toList()
        );
    }

    private UsuarioResponseDTO mapearUsuario(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getCriadoEm(),
                usuario.getAtualizadoEm()
        );
    }

    private TarefaResponseDTO mapearTarefa(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus().name(),
                tarefa.getUsuario().getEmail()
        );
    }
}
