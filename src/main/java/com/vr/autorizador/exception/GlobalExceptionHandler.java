package com.vr.autorizador.exception;

import com.vr.autorizador.dto.CartaoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartaoExisteException.class)
    public ResponseEntity<CartaoResponseDTO> handleCartaoExiste(CartaoExisteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getCartaoResponse());
    }

    @ExceptionHandler(TransacaoInvalidaException.class)
    public ResponseEntity<String> handleTransacaoInvalida(TransacaoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMotivo().name());
    }
}
