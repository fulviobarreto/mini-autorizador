# Mini Autorizador

O **Mini Autorizador** é uma aplicação backend estruturada para processar o cadastro de cartões e a autorização de transações financeiras em tempo real. O sistema garante robustez contra concorrência de requisições simultâneas e adota um paradigma de programação funcional em Java moderno, eliminando o uso de instruções condicionais (`if`).

## Índice

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Decisões de Projeto e Arquitetura](#decisões-de-projeto-e-arquitetura)
- [Estrutura de pacotes](#estrutura-de-pacotes)
- [Modelo de dados](#modelo-de-dados)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Endpoints da API](#endpoints-da-api)
- [Como executar](#como-executar)
- [Testes](#testes)
- [Autor](#autor)

## Funcionalidades

- **Cadastro de Cartões**: Criação de novos cartões com saldo inicial padrão e geração dinâmica de senha.
- **Consulta de Saldo**: Recuperação do saldo atual associado a um número de cartão válido.
- **Processamento de Transações**: Validação de regras de negócio (cartão existente, senha válida e saldo suficiente).
- **Tratamento de Concorrência**: Bloqueio pessimista de registros no banco de dados para evitar condições de corrida (*race conditions*).
- **Tratamento Global de Exceções**: Respostas padronizadas para cenários de erro de negócio (ex: saldo insuficiente, cartão já existente, senha inválida).

## Tecnologias

| Categoria                    | Tecnologia                          |
|-------------------------------|--------------------------------------|
| Linguagem                     | Java 21                          |
| Framework                     | Spring Boot 4.x                     |
| Segurança                     | Spring Security (HTTP Basic Auth)   |
| Persistência                  | Spring Data JPA / Hibernate         |
| Banco de dados                | MySQL                               |
| Redução de Boilerplate        | Project Lombok                      |
| Gerenciador de dependências   | Maven                               |
| Testes                        | JUnit 5, Mockito, Spring Security Test|

## Decisões de Projeto e Arquitetura

O projeto foi construído priorizando as melhores práticas de engenharia de software corporativa:

1. **Zero Condicionais (`if`):** As validações de regras de negócio em serviços e fluxos de domínio foram implementadas utilizando recursos funcionais nativos do Java (`Optional.filter`, `.map` e `.orElseThrow`), mantendo o código linear, declarativo e legível.
2. **Geração Dinâmica de Senha:** A regra de senha padrão extrai de forma programática e segura os últimos 4 dígitos do número do cartão informado na requisição, sem requerer persistência de campos extras desnecessários no DTO de entrada.
3. **Concorrência com Query Nativa e Bloqueio Pessimista (`FOR UPDATE`):** Para garantir a consistência estrita do saldo durante transações financeiras paralelas de alta concorrência, o repositório utiliza uma query nativa otimizada com a instrução `FOR UPDATE` do MySQL, assegurando isolamento transacional rigoroso sem conflitos de dialeto ORM.
4. **Arquitetura em Camadas:** Separação clara entre Controllers (orquestração HTTP), Services (regras de negócio e fluxos funcionais), Repositories (comunicação com ORM) e DTOs/Entities (transporte de dados e mapeamento relacional).

## Estrutura de pacotes
```
com.vr.miniautorizador
├── controller
│   ├── CartaoController
│   └── TransacaoController
├── domain / entity
│   ├── Cartao
│   └── Transacao
├── dto
│   ├── CartaoResponse
│   ├── CriarCartaoRequest
│   └── TransacaoDTO
├── enums
│   └── RegraAutorizacaoEnum
├── exception
│   ├── CartaoJaExisteException
│   ├── GlobalExceptionHandler
│   └── TransacaoInvalidaException
├── repository
│   └── CartaoRepository
├── service
│   ├── CartaoService
│   └── TransacaoService
└── MiniAutorizadorApplication
```

## Modelo de dados

A tabela principal de **Cartão** armazena o número identificador único, a senha gerada e o saldo atual, servindo de base para o processamento atômico das transações.

## Segurança e Autenticação

A API utiliza **Spring Security** configurado com autenticação **HTTP Basic**, garantindo que apenas usuários autenticados possam acessar os endpoints de escrita e consulta.

## Endpoints da API

| Método | Endpoint                       | Descrição                          | Status de Sucesso |
|--------|----------------------------------|-------------------------------------|-------------------|
| POST   | `/cartoes`                      | Cadastra um novo cartão             | `201 CREATED`     |
| GET    | `/cartoes/{numeroCartao}`       | Consulta o saldo atual do cartão    | `200 OK`          |
| POST   | `/transacoes`                   | Processa e autoriza uma transação   | `201 CREATED` (`OK`) |

### Exemplos de Requisição

#### Criar Cartão (`POST http://localhost:8080/cartoes`)
**Requisição:**
```json
Body
{
  "numeroCartao": "6549873025634501"
}
```
#### Realizar Transação (`POST http://localhost:8080/transacoes`)
**Requisição:**
```json
Body
{
  "numeroCartao": "6549873025634501",
  "senhaCartao": "4501",
  "valor": 10.00
}
```

### Testes
O projeto conta com uma suíte de testes automatizados abrangente cobrindo:

**Testes Unitários de Serviço** (`CartaoServiceTest`, `TransacaoServiceTest`): Validação isolada de regras de negócio, geração de senhas e cenários de exceção.

**Testes de Integração Web** (`CartaoControllerTest`, `TransacaoControllerTest`): Validação de endpoints via MockMvc, checagem de contratos JSON e segurança HTTP Basic.

Para executar todos os testes via linha de comando:
```bash
./mvnw test
```
