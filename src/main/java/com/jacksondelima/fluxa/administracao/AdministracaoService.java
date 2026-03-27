package com.jacksondelima.fluxa.administracao;

import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.TarefaRepository;
import com.jacksondelima.fluxa.tarefa.dto.TarefaResponseDTO;
import com.jacksondelima.fluxa.usuario.Perfil;
import com.jacksondelima.fluxa.usuario.UsuarioRepository;
import com.jacksondelima.fluxa.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdministracaoService {

    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    public Page<UsuarioResponseDTO> listarUsuarios(String termo, Perfil perfil, Pageable pageable) {
        return usuarioRepository.buscarParaAdministracao(normalizarTexto(termo), perfil, pageable);
    }

    public Page<TarefaResponseDTO> listarTarefas(
            String termo,
            StatusTarefa status,
            String emailUsuario,
            Pageable pageable
    ) {
        return tarefaRepository.buscarParaAdministracao(
                normalizarTexto(termo),
                status,
                normalizarEmail(emailUsuario),
                pageable
        );
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
