package com.vr.autorizador.service;

import com.vr.autorizador.model.Cartao;
import com.vr.autorizador.dto.CartaoResponseDTO;
import com.vr.autorizador.exception.CartaoExisteException;
import com.vr.autorizador.repository.CartaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartaoService {

    private static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");
    private final CartaoRepository cartaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CartaoResponseDTO criar(String numeroCartao, String senha) {
        cartaoRepository.findByNumeroCartao(numeroCartao)
                // Se o cartão já existe, lança a exceção contendo a senha e o numeroCartao cadastrados
                .ifPresent(cartao -> {
                    throw new CartaoExisteException(new CartaoResponseDTO(senha, numeroCartao));
                });

        try{
            Cartao novoCartao = Cartao.builder()
                    .numeroCartao(numeroCartao)
                    .senha(passwordEncoder.encode(senha)) // Armazena o hash criptografado no banco
                    .saldo(SALDO_INICIAL)
                    .build();
            cartaoRepository.saveAndFlush(novoCartao);
        } catch (DataIntegrityViolationException ex) {
            // Caso ocorra concorrência extrema e o banco recuse por unique constraint.
            throw new CartaoExisteException(new CartaoResponseDTO(senha, numeroCartao));
        }

        return new CartaoResponseDTO(senha, numeroCartao);
    }

    @Transactional(readOnly = true)
    public BigDecimal obterSaldo(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao)
                .map(Cartao::getSaldo)
                .orElse(null);
    }
}