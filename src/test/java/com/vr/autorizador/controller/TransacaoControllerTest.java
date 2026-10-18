package com.vr.autorizador.controller;

import com.vr.autorizador.dto.TransacaoDTO;
import com.vr.autorizador.enums.RegraAutorizacaoEnum;
import com.vr.autorizador.exception.TransacaoInvalidaException;
import com.vr.autorizador.service.TransacaoService;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@AutoConfigureMockMvc
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoService transacaoService;

    @Test
    @DisplayName("Realizar transação com sucesso, retornar 201 e a string OK")
    @WithMockUser
    void realizarTransacaoComSucesso() throws Exception {
        TransacaoDTO dto = new TransacaoDTO("6549873025634501", "4501", new BigDecimal("10.00"));

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("POST /transacoes - Retornar 422 com SALDO_INSUFICIENTE")
    @WithMockUser
    void retornarSaldoInsuficiente() throws Exception {
        TransacaoDTO dto = new TransacaoDTO("6549873025634501", "4501", new BigDecimal("600.00"));

        // Simula o service lançando a exceção de negócio para saldo insuficiente
        doThrow(new TransacaoInvalidaException(RegraAutorizacaoEnum.SALDO_INSUFICIENTE))
                .when(transacaoService).realizarTransacao(dto.numeroCartao(), dto.senhaCartao(), dto.valor());

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(content().string(RegraAutorizacaoEnum.SALDO_INSUFICIENTE.name()));
    }

    @Test
    @DisplayName("POST /transacoes - Retornar 400 Bad Request quando o valor da transação for nulo ou negativo")
    @WithMockUser
    void transacaoValorInvalido() throws Exception {
        TransacaoDTO dto = new TransacaoDTO("6549873025634501", "4501", new BigDecimal("-10.00"));

        mockMvc.perform(post("/transacoes")
                        .with(httpBasic("username", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}