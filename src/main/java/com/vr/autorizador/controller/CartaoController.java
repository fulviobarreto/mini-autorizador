package com.vr.autorizador.controller;

import com.vr.autorizador.dto.CartaoRequestDTO;
import com.vr.autorizador.dto.CartaoResponseDTO;
import com.vr.autorizador.service.CartaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService cartaoService;

    @PostMapping
    public ResponseEntity<CartaoResponseDTO> criar(@Valid @RequestBody CartaoRequestDTO request) {
        CartaoResponseDTO cartaoCriado = cartaoService.criar(request.numeroCartao());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartaoCriado);
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> obterSaldo(@PathVariable String numeroCartao) {
        return Optional.ofNullable(cartaoService.obterSaldo(numeroCartao))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}