package com.jacksondelima.fluxa.tarefa;

import com.jacksondelima.fluxa.tarefa.dto.TarefaRequestDTO;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Usuario;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public TarefaResponseDTO criar(TarefaRequestDTO request) {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado();

        Tarefa tarefa = Tarefa.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .status(StatusTarefa.PENDENTE)
                .usuario(usuarioAutenticado)
                .build();

        return mapearResposta(tarefaRepository.save(tarefa));
    }

    public List<TarefaResponseDTO> listarMinhas() {
        Usuario usuarioAutenticado = buscarUsuarioAutenticado();

        return tarefaRepository.findByUsuario(usuarioAutenticado)
                .stream()
                .map(this::mapearResposta)
                .toList();
    }

    public TarefaResponseDTO buscarPorId(Long id) {
        Usuario usuario = buscarUsuarioAutenticado();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario);
        return mapearResposta(tarefa);
    }

    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO request) {
        Usuario usuario = buscarUsuarioAutenticado();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario);

        tarefa.setTitulo(request.titulo());
        tarefa.setDescricao(request.descricao());

        return mapearResposta(tarefaRepository.save(tarefa));
    }

    public void excluir(Long id) {
        Usuario usuario = buscarUsuarioAutenticado();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario);
        tarefaRepository.delete(tarefa);
    }

    private Usuario buscarUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado."));
    }

    private Tarefa buscarTarefaDoUsuario(Long id, Usuario usuario) {
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada."));

        if (!tarefa.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Acesso negado para esta tarefa.");
        }

        return tarefa;
    }

    private TarefaResponseDTO mapearResposta(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus().name(),
                tarefa.getUsuario().getEmail()
        );
    }
}
