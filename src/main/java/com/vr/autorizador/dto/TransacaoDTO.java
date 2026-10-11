package com.vr.autorizador.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransacaoDTO(
        @NotBlank(message = "O número do cartão não pode estar em branco")
        @Size(min = 16, max = 16, message = "O número do cartão deve conter exatamente 16 dígitos")
        @Pattern(regexp = "\\d+", message = "O número do cartão deve conter apenas números")
        String numeroCartao,

        @NotBlank(message = "A senha do cartão não pode estar em branco")
        @Size(min = 4, max = 4, message = "A senha deve conter exatamente 4 dígitos")
        @Pattern(regexp = "\\d+", message = "A senha deve conter apenas números")
        String senhaCartao,

        @NotNull(message = "O valor da transação não pode ser nulo")
        @DecimalMin(value = "0.01", message = "O valor da transação deve ser maior que zero")
        BigDecimal valor
) {}
