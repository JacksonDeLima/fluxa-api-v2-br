package com.jacksondelima.fluxa.autenticacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequestDTO(
        @NotBlank(message = "O nome e obrigatorio.")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "E-mail invalido.")
        String email,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String senha
) {
}
