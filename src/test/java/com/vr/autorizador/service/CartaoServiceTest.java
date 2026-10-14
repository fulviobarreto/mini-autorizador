package com.vr.autorizador.service;

import com.vr.autorizador.model.Cartao;
import com.vr.autorizador.dto.CartaoResponseDTO;
import com.vr.autorizador.dto.CartaoRequestDTO;
import com.vr.autorizador.exception.CartaoExisteException;
import com.vr.autorizador.repository.CartaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    @Test
    @DisplayName("Criar cartão com sucesso e gerar senha com os 4 últimos dígitos do número")
    void criarCartaoComSucesso() {
        CartaoRequestDTO request = new CartaoRequestDTO("6549873025634501", "4501");

        when(cartaoRepository.findByNumeroCartao(request.numeroCartao()))
                .thenReturn(Optional.empty());

        CartaoResponseDTO response = cartaoService.criar(request.numeroCartao(), request.senha());

        assertNotNull(response);
        assertEquals("6549873025634501", response.numeroCartao());
        assertEquals("4501", response.senha()); // Últimos 4 dígitos extraídos dinamicamente
        verify(cartaoRepository, times(1)).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Lançar CartaoExisteException quando o cartão já estiver cadastrado")
    void lancarExcecaoQuandoCartaoExistir() {
        CartaoRequestDTO request = new CartaoRequestDTO("6549873025634501", "4501");

        Cartao cartaoExistente = Cartao.builder()
                .id(1L)
                .numeroCartao("6549873025634501")
                .senha("4501")
                .saldo(new BigDecimal("500.00"))
                .build();

        when(cartaoRepository.findByNumeroCartao(request.numeroCartao()))
                .thenReturn(Optional.of(cartaoExistente));

        CartaoExisteException exception = assertThrows(
                CartaoExisteException.class,
                () -> cartaoService.criar(request.numeroCartao(), request.senha())
        );

        assertNotNull(exception.getCartaoResponse());
        assertEquals("6549873025634501", exception.getCartaoResponse().numeroCartao());
        assertEquals("4501", exception.getCartaoResponse().senha());
        verify(cartaoRepository, never()).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Retornar o saldo atual do cartão com sucesso")
    void obterSaldoComSucesso() {
        String numeroCartao = "6549873025634501";
        Cartao cartao = Cartao.builder()
                .id(1L)
                .numeroCartao(numeroCartao)
                .senha("4501")
                .saldo(new BigDecimal("500.00"))
                .build();

        when(cartaoRepository.findByNumeroCartao(numeroCartao))
                .thenReturn(Optional.of(cartao));

        BigDecimal saldo = cartaoService.obterSaldo(numeroCartao);

        assertNotNull(saldo);
        assertEquals(new BigDecimal("500.00"), saldo);
    }

    @Test
    @DisplayName("Retornar null quando o cartão não for encontrado na consulta de saldo")
    void retornarNullQuandoCartaoNaoEncontrado() {
        String numeroCartao = "9999999999999999";

        when(cartaoRepository.findByNumeroCartao(numeroCartao))
                .thenReturn(Optional.empty());

        BigDecimal saldo = cartaoService.obterSaldo(numeroCartao);

        assertNull(saldo);
    }
}