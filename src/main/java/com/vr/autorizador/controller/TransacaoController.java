package com.vr.autorizador.controller;

import com.vr.autorizador.dto.TransacaoDTO;
import com.vr.autorizador.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<String> realizarTransacao(@Valid @RequestBody TransacaoDTO dto) {
        transacaoService.realizarTransacao(dto.numeroCartao(), dto.senhaCartao(), dto.valor());
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}
