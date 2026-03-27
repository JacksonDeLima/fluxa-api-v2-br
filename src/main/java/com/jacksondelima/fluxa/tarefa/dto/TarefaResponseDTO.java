package com.jacksondelima.fluxa.tarefa.dto;

import com.jacksondelima.fluxa.tarefa.StatusTarefa;
import com.jacksondelima.fluxa.tarefa.Tarefa;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        String emailUsuario
) {

    public static TarefaResponseDTO from(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getUsuario().getEmail()
        );
    }
}
