package com.jacksondelima.fluxa.tarefa;

import com.jacksondelima.fluxa.excecao.RecursoNaoEncontradoException;
import com.jacksondelima.fluxa.seguranca.UsuarioAutenticadoService;
import com.jacksondelima.fluxa.tarefa.dto.TarefaRequestDTO;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public TarefaResponseDTO criar(TarefaRequestDTO request) {
        Usuario usuarioAutenticado = usuarioAutenticadoService.buscar();

        Tarefa tarefa = Tarefa.builder()
                .titulo(request.titulo().trim())
                .descricao(request.descricao().trim())
                .status(StatusTarefa.PENDENTE)
                .usuario(usuarioAutenticado)
                .build();

        return TarefaResponseDTO.from(tarefaRepository.save(tarefa));
    }

    public List<TarefaResponseDTO> listarMinhas() {
        Usuario usuarioAutenticado = usuarioAutenticadoService.buscar();

        return tarefaRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioAutenticado.getId())
                .stream()
                .map(TarefaResponseDTO::from)
                .toList();
    }

    public TarefaResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioAutenticadoService.buscar();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario.getId());
        return TarefaResponseDTO.from(tarefa);
    }

    @Transactional
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO request) {
        Usuario usuario = usuarioAutenticadoService.buscar();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario.getId());

        tarefa.setTitulo(request.titulo().trim());
        tarefa.setDescricao(request.descricao().trim());

        return TarefaResponseDTO.from(tarefaRepository.save(tarefa));
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = usuarioAutenticadoService.buscar();
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuario.getId());
        tarefaRepository.delete(tarefa);
    }

    private Tarefa buscarTarefaDoUsuario(Long id, Long usuarioId) {
        return tarefaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa nao encontrada."));
    }
}
