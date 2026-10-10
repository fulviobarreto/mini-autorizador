package com.vr.autorizador.dto;

import java.math.BigDecimal;

public record TransacaoDTO(String numeroCartao, String senhaCartao, BigDecimal valor) {
}
