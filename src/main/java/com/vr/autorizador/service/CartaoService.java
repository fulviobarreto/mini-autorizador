package com.vr.autorizador.service;

import com.vr.autorizador.model.Cartao;
import com.vr.autorizador.dto.CartaoResponseDTO;
import com.vr.autorizador.exception.CartaoExisteException;
import com.vr.autorizador.repository.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private static final String SENHA_PADRAO = "1234";
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");
    private final CartaoRepository cartaoRepository;

    @Transactional
    public CartaoResponseDTO criar(String numeroCartao) {
        cartaoRepository.findByNumeroCartao(numeroCartao)
                // Se o cartão já existe, lança a exceção contendo a senha e o numeroCartao cadastrados
                .ifPresent(cartao -> {
                    throw new CartaoExisteException(new CartaoResponseDTO(cartao.getSenha(), cartao.getNumeroCartao()));
                });

        Cartao novoCartao = Cartao.builder()
                .numeroCartao(numeroCartao)
                .senha(senhaGerada(numeroCartao))
                .saldo(SALDO_INICIAL)
                .build();
        cartaoRepository.save(novoCartao);

        return new CartaoResponseDTO(novoCartao.getSenha(), novoCartao.getNumeroCartao());
    }

    @Transactional(readOnly = true)
    public BigDecimal obterSaldo(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao)
                .map(Cartao::getSaldo)
                .orElse(null);
    }

    /**
     * Define a senha padrão exigida no contrato
     */
    private String senhaGerada(String numeroCartao) {
        return Optional.ofNullable(numeroCartao)
                .filter(num -> num.length() >= 4)
                .map(num -> num.substring(num.length() - 4))
                .orElse(SENHA_PADRAO); // Valor fallback caso a entrada seja nula ou menor que 4 caracteres
    }
}