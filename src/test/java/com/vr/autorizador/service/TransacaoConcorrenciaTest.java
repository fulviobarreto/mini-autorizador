package com.vr.autorizador.service;

import com.vr.autorizador.model.Cartao;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoConcorrenciaTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Deve garantir consistência transacional sob concorrência de múltiplas requisições paralelas")
    void garantirConsistenciaEmTransacoesConcorrentes() throws InterruptedException {
        String numeroCartao = "6549873025634501";
        Cartao cartao = Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("$2a$10$hashedPassword")
                .saldo(new BigDecimal("50.00"))
                .build();

        when(cartaoRepository.findByNumeroCartaoForUpdate(numeroCartao)).thenReturn(Optional.of(cartao));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        int numeroThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numeroThreads);
        CountDownLatch latch = new CountDownLatch(numeroThreads);

        // Dispara 5 transações paralelas de R$ 10,00 cada (total R$ 50,00, esgotando o saldo)
        for (int i = 0; i < numeroThreads; i++) {
            executorService.submit(() -> {
                try {
                    transacaoService.realizarTransacao(numeroCartao, "1234", new BigDecimal("10.00"));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // O saldo final deve ser exatamente 0.00 após as operações simultâneas
        assertEquals(0, new BigDecimal("0.00").compareTo(cartao.getSaldo()));
        verify(cartaoRepository, times(numeroThreads)).save(cartao);
    }
}