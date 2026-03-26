package com.jacksondelima.fluxa.tarefa.dto;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        String status,
        String emailUsuario
) {
}
