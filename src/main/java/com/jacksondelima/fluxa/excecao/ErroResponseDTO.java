package com.jacksondelima.fluxa.excecao;

import java.time.Instant;

public record ErroResponseDTO(
        String erro,
        String mensagem,
        int status,
        String caminho,
        Instant timestamp
) {
}
