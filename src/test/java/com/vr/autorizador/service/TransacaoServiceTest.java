package com.vr.autorizador.service;

import com.vr.autorizador.model.Cartao;
import com.vr.autorizador.enums.RegraAutorizacaoEnum;
import com.vr.autorizador.exception.TransacaoInvalidaException;
import com.vr.autorizador.repository.CartaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Autorizar e debitar saldo com sucesso quando todas as validações forem satisfeitas")
    void realizarTransacaoComSucesso() {
        String numeroCartao = "6549873025634501";
        String senhaCartao = "4501";
        BigDecimal valor = new BigDecimal("100.00");

        Cartao cartaoValido = Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("$2a$10$encodedPasswordHash")
                .saldo(new BigDecimal("500.00"))
                .build();

        when(cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao))
                .thenReturn(Optional.of(cartaoValido));
        when(passwordEncoder.matches(eq(senhaCartao), anyString()))
                .thenReturn(true);

        transacaoService.realizarTransacao(numeroCartao, senhaCartao, valor);

        assertEquals(new BigDecimal("400.00"), cartaoValido.getSaldo());
        verify(cartaoRepository, times(1)).save(cartaoValido);
    }

    @Test
    @DisplayName("Lançar exceção CARTAO_INEXISTENTE quando o cartão não for encontrado")
    void lancarExcecaoQuandoCartaoNaoExistir() {
        String numeroCartao = "9999999999999999";
        String senhaCartao = "1234";
        BigDecimal valor = new BigDecimal("10.00");

        when(cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao))
                .thenReturn(Optional.empty());

        TransacaoInvalidaException exception = assertThrows(
                TransacaoInvalidaException.class,
                () -> transacaoService.realizarTransacao(numeroCartao, senhaCartao, valor)
        );

        assertEquals(RegraAutorizacaoEnum.CARTAO_INEXISTENTE, exception.getMotivo());
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lançar exceção SENHA_INVALIDA quando a senha fornecida for incorreta")
    void lancarExcecaoSenhaInvalida() {
        String numeroCartao = "6549873025634501";
        String senhaCartao = "0000";
        BigDecimal valor = new BigDecimal("10.00");

        Cartao cartao = Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("$2a$10$encodedPasswordHash")
                .saldo(new BigDecimal("500.00"))
                .build();

        when(cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao))
                .thenReturn(Optional.of(cartao));
        when(passwordEncoder.matches(eq(senhaCartao), anyString()))
                .thenReturn(false);

        TransacaoInvalidaException exception = assertThrows(
                TransacaoInvalidaException.class,
                () -> transacaoService.realizarTransacao(numeroCartao, senhaCartao, valor)
        );

        assertEquals(RegraAutorizacaoEnum.SENHA_INVALIDA, exception.getMotivo());
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lançar exceção SALDO_INSUFICIENTE quando o valor da transação for maior que o saldo")
    void lancarExcecaoSaldoForInsuficiente() {
        String numeroCartao = "6549873025634501";
        String senhaCartao = "4501";
        BigDecimal valor = new BigDecimal("600.00");

        Cartao cartao = Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("$2a$10$encodedPasswordHash")
                .saldo(new BigDecimal("500.00"))
                .build();

        when(cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao))
                .thenReturn(Optional.of(cartao));
        when(passwordEncoder.matches(eq(senhaCartao), anyString()))
                .thenReturn(true);

        TransacaoInvalidaException exception = assertThrows(
                TransacaoInvalidaException.class,
                () -> transacaoService.realizarTransacao(numeroCartao, senhaCartao, valor)
        );

        assertEquals(RegraAutorizacaoEnum.SALDO_INSUFICIENTE, exception.getMotivo());
        verify(cartaoRepository, never()).save(any());
    }
}