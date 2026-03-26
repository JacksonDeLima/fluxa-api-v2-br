package com.jacksondelima.fluxa.tarefa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TarefaRequestDTO(
        @NotBlank(message = "O titulo e obrigatorio.")
        @Size(max = 255, message = "O titulo deve ter no maximo 255 caracteres.")
        String titulo,

        @NotBlank(message = "A descricao e obrigatoria.")
        String descricao
) {
}
