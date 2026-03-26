package com.jacksondelima.fluxa.tarefa;

import com.jacksondelima.fluxa.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByUsuario(Usuario usuario);
    List<Tarefa> findByUsuarioAndStatus(Usuario usuario, StatusTarefa status);
    List<Tarefa> findByStatus(StatusTarefa status);
}
