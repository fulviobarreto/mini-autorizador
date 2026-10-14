package com.vr.autorizador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CartaoRequestDTO(
        @NotBlank(message = "O número do cartão não pode estar em branco")
        @Size(min = 16, max = 16, message = "O número do cartão deve conter exatamente 16 dígitos")
        @Pattern(regexp = "\\d+", message = "O número do cartão deve conter apenas números")
        String numeroCartao,

        @NotBlank(message = "A senha do cartão não pode estar em branco")
        @Size(min = 4, max = 4, message = "A senha deve conter exatamente 4 dígitos")
        @Pattern(regexp = "\\d+", message = "A senha deve conter apenas números")
        String senha
) {}
