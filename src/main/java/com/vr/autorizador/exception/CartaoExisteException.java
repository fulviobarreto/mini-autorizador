package com.vr.autorizador.exception;

import com.vr.autorizador.dto.CartaoResponseDTO;
import lombok.Getter;

@Getter
public class CartaoExisteException extends RuntimeException {
    private final CartaoResponseDTO cartaoResponse;

    public CartaoExisteException(CartaoResponseDTO cartaoResponse) {
        this.cartaoResponse = cartaoResponse;
    }
}
