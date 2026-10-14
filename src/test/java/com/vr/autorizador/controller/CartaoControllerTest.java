package com.vr.autorizador.controller;

import com.vr.autorizador.dto.CartaoResponseDTO;
import com.vr.autorizador.dto.CartaoRequestDTO;
import com.vr.autorizador.exception.CartaoExisteException;
import com.vr.autorizador.service.CartaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartaoController.class)
@AutoConfigureMockMvc
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartaoService cartaoService;

    @Test
    @DisplayName("POST /cartoes - Criar cartão com sucesso")
    @WithMockUser
    void criarCartao() throws Exception {
        CartaoRequestDTO request = new CartaoRequestDTO("6549873025634501","4501");

        CartaoResponseDTO response = new CartaoResponseDTO("4501", "6549873025634501");

        when(cartaoService.criar(request.numeroCartao(), request.senha())).thenReturn(response);

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
                .andExpect(jsonPath("$.senha").value("4501"));
    }

    @Test
    @DisplayName("POST /cartoes - Retornar 422 e dados do cartão existente quando o cartão já estiver cadastrado")
    @WithMockUser
    void consultaCartaoExistente() throws Exception {
        CartaoRequestDTO request = new CartaoRequestDTO("6549873025634501", "4501");

        CartaoResponseDTO response = new CartaoResponseDTO("4501", "6549873025634501");

        when(cartaoService.criar(any(String.class), any(String.class)))
                .thenThrow(new CartaoExisteException(response));

        mockMvc.perform(post("/cartoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
                .andExpect(jsonPath("$.senha").value("4501"));
    }

    @Test
    @DisplayName("GET /cartoes/{numeroCartao} - Retornar o saldo atual do cartão com sucesso e 200 OK")
    @WithMockUser
    void obterSaldoComSucesso() throws Exception {
        String numeroCartao = "6549873025634501";
        BigDecimal saldoEsperado = new BigDecimal("500.00");

        when(cartaoService.obterSaldo(eq(numeroCartao))).thenReturn(saldoEsperado);

        mockMvc.perform(get("/cartoes/{numeroCartao}", numeroCartao)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    @DisplayName("GET /cartoes/{numeroCartao} - Retornar 404 Not Found quando o cartao nao for encontrado")
    @WithMockUser
    void cartaoNaoEncontrado() throws Exception {
        String numeroCartao = "9999999999999999";

        when(cartaoService.obterSaldo(eq(numeroCartao))).thenReturn(null);

        mockMvc.perform(get("/cartoes/{numeroCartao}", numeroCartao)
                        .with(httpBasic("username", "password")))
                .andExpect(status().isNotFound());
    }
}