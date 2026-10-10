package com.vr.autorizador.service;

import com.vr.autorizador.enums.RegraAutorizacaoEnum;
import com.vr.autorizador.exception.TransacaoInvalidaException;
import com.vr.autorizador.model.Cartao;
import com.vr.autorizador.repository.CartaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final CartaoRepository cartaoRepository;

    @Transactional
    public void realizarTransacao(String numeroCartao, String senhaCartao, BigDecimal valor) {
        // Regra 1: O cartão deve existir
        Cartao cartao = cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao)
                .orElseThrow(() -> new TransacaoInvalidaException(RegraAutorizacaoEnum.CARTAO_INEXISTENTE));

        // Regra 2: A senha deve ser a correta
        Optional.of(cartao)
                .filter(c -> c.getSenha().equals(senhaCartao))
                .orElseThrow(() -> new TransacaoInvalidaException(RegraAutorizacaoEnum.SENHA_INVALIDA));

        // Regra 3: O cartão deve possuir saldo suficiente
        Optional.of(cartao)
                .filter(c -> c.getSaldo().compareTo(valor) >= 0)
                .orElseThrow(() -> new TransacaoInvalidaException(RegraAutorizacaoEnum.SALDO_INSUFICIENTE));

        // Debita o saldo e atualiza no banco
        cartao.setSaldo(cartao.getSaldo().subtract(valor));
        cartaoRepository.save(cartao);
    }
}
