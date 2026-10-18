# Mini Autorizador

O **Mini Autorizador** é uma aplicação backend de alta performance construída para processar o cadastro de cartões, consultas de saldo e a autorização de transações financeiras em tempo real. O sistema foi projetado para ambientes de altíssima concorrência, adotando um paradigma de programação funcional declarativo sem o uso de instruções condicionais (`if`), além de proteção transacional rigorosa e criptografia BCrypt.

## Índice

* [Funcionalidades](#funcionalidades)
* [Tecnologias](#tecnologias)
* [Premissas de Projeto e Arquitetura](#premissas-de-projeto-e-arquitetura)
* [Segurança e Autenticação](#segurança-e-autenticação)
* [Tratamento de Concorrência e Transacionalidade](#tratamento-de-concorrência-e-transacionalidade)
* [Estrutura do Projeto](#estrutura-do-projeto)
* [Endpoints da API e Exemplos de Contrato](#endpoints-da-api-e-exemplos-de-contrato)
* [Como Executar a Aplicação](#como-executar-a-aplicação)
* [Suíte de Testes Automatizados](#suíte-de-testes-automatizados)
* [Histórico Evolutivo e Commits do Projeto](#histórico-evolutivo-e-commits-do-projeto)

## Funcionalidades

* **Cadastro de Cartões**: Criação de novos cartões com saldo inicial automático de `R$ 500,00`, recebendo número e senha.
* **Consulta de Saldo**: Recuperação do saldo atual associado a um cartão cadastrado.
* **Autorização de Transações**: Processamento atômico de débito com validação em cascata (existência do cartão, senha correta e saldo suficiente).
* **Tratamento Resiliente de Erros**: Respostas de erro padronizadas e mapeadas para status HTTP adequados (`400 Bad Request`, `401 Unauthorized`, `404 Not Found` e `422 Unprocessable Entity`).

## Tecnologias

| Categoria | Tecnologia |
| ----- | ----- |
| Linguagem | Java 21 |
| Framework | Spring Boot 4.x |
| Segurança | Spring Security (HTTP Basic & BCrypt) |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | MySQL 5.7 (via Docker Compose) |
| Redução de Boilerplate | Project Lombok |
| Gerenciador de Dependências | Maven |
| Testes | JUnit 5, Mockito, Spring Security Test |

## Premissas de Projeto e Arquitetura

O projeto adota padrões de engenharia de software voltados para robustez, segurança e legibilidade:

1. **Ausência Absoluta de Instruções Condicionais (`if`):**
   Toda a camada de serviço e validações de fluxo de negócio foram implementadas utilizando o paradigma funcional nativo do Java (`Optional.filter`, `.map` e `.orElseThrow`). Isso elimina ramificações manuais de código, garantindo um fluxo declarativo, limpo e à prova de falhas de lógica.

2. **Criptografia de Senhas com BCrypt:**
   Por premissa de segurança e conformidade com boas práticas (OWASP), nenhuma senha de cartão é armazenada em texto plano no banco de dados. As senhas são codificadas usando `BCryptPasswordEncoder` no momento do cadastro e validadas via comparação de hash (`passwordEncoder.matches`) durante a autorização de transações.

3. **Externalização Total de Credenciais da API:**
   A segurança da API via HTTP Basic utiliza propriedades parametrizadas e lidas em tempo de execução (`app.security.user.name` e `app.security.user.password`), evitando *hardcode* de credenciais e garantindo compatibilidade com ambientes de integração contínua (CI/CD).

4. **Arquitetura em Camadas Isoladas:**
   * **Controller:** Exposição de endpoints REST, validação sintática de payloads (`@Valid`) e mapeamento de respostas HTTP.
   * **Service:** Regras de negócio funcionais, gerenciamento transacional e criptografia.
   * **Repository:** Consultas otimizadas com suporte a bloqueio em nível de linha.
   * **Exception Handler:** Tratamento global e centralizado de exceções de domínio e concorrência.

## Segurança e Autenticação

A API é protegida por **Spring Security** utilizando o esquema **HTTP Basic Auth**.

* **Usuário padrão de API (`app.security.user.name`):** `username`
* **Senha padrão de API (`app.security.user.password`):** `password`

Todas as requisições enviadas aos endpoints `/cartoes` e `/transacoes` devem incluir o cabeçalho HTTP:

```http
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
```

## Tratamento de Concorrência e Transacionalidade

Para evitar condições de corrida (*race conditions*) em transações financeiras simultâneas (ex: duas tentativas de débito concorrentes no mesmo cartão), a camada de persistência utiliza **Bloqueio Pessimista (`FOR UPDATE`)**:

* A consulta de autorização executa uma instrução nativa `SELECT ... FOR UPDATE` no MySQL (`findByNumeroCartaoForUpdate`).
* A linha correspondente ao cartão fica bloqueada no banco de dados até o término da transação `@Transactional`.
* Transações simultâneas no mesmo cartão aguardam em fila a liberação do lock, garantindo a atomicidade e a consistência estrita do saldo final.
* Tentativas de criação de cartões simultâneos com o mesmo número são tratadas atomicamente no banco de dados e capturadas pela camada de serviço, retornando status `422 Unprocessable Entity` com resiliência.

## Estrutura do Projeto

```text
com.vr.autorizador
├── config
│   └── SecurityConfig.java
├── controller
│   ├── CartaoController.java
│   └── TransacaoController.java
├── dto
│   ├── CartaoRequestDTO.java
│   ├── CartaoResponseDTO.java
│   └── TransacaoDTO.java
├── enums
│   └── RegraAutorizacaoEnum.java
├── exception
│   ├── CartaoExisteException.java
│   ├── GlobalExceptionHandler.java
│   └── TransacaoInvalidaException.java
├── model
│   └── Cartao.java
├── repository
│   └── CartaoRepository.java
└── service
    ├── CartaoService.java
    └── TransacaoService.java
```

## Endpoints da API e Exemplos de Contrato

### 1. Criar Cartão

Cadastra um novo cartão com saldo inicial de `500.00`.

* **Método:** `POST`
* **URL:** `/cartoes`
* **Status Sucesso:** `201 CREATED`
* **Status Cartão Existente:** `422 UNPROCESSABLE_ENTITY`

**Body da Requisição:**

```json
{
  "numeroCartao": "6549873025634501",
  "senha": "1234"
}
```

**Resposta de Sucesso (`201 CREATED`):**

```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```

**Resposta quando o Cartão já Existe (`422 UNPROCESSABLE_ENTITY`):**

```json
{
  "senha": "1234",
  "numeroCartao": "6549873025634501"
}
```

### 2. Obter Saldo do Cartão

Retorna o saldo corrente do cartão informado.

* **Método:** `GET`
* **URL:** `/cartoes/{numeroCartao}`
* **Status Sucesso:** `200 OK` (retorna o valor decimal no corpo)
* **Status Não Encontrado:** `404 NOT FOUND`

**Exemplo de Chamada:** `GET /cartoes/6549873025634501`

**Resposta de Sucesso (`200 OK`):**

```text
500.00
```

### 3. Realizar Transação

Processa um débito no cartão mediante validação de existência, senha e saldo.

* **Método:** `POST`
* **URL:** `/transacoes`
* **Status Sucesso:** `201 CREATED` (corpo: `OK`)
* **Status Falha de Negócio:** `422 UNPROCESSABLE_ENTITY`

**Body da Requisição:**

```json
{
  "numeroCartao": "6549873025634501",
  "senhaCartao": "1234",
  "valor": 10.00
}
```

**Resposta de Sucesso (`201 CREATED`):**

```text
OK
```

**Respostas de Erro de Negócio (`422 UNPROCESSABLE_ENTITY`):**

* `SALDO_INSUFICIENTE`
* `SENHA_INVALIDA`
* `CARTAO_INEXISTENTE`

## Como Executar a Aplicação

### Pré-requisitos

* Java 21 instalado e configurado.
* Docker e Docker Compose instalados.
* Maven 3.8+ (ou o wrapper `./mvnw` do projeto).

---

### 1. Subir o Banco de Dados MySQL via Docker Compose

O projeto dispõe de um container MySQL 5.7 pré-configurado no arquivo `docker-compose.yml`.

Para iniciar o banco de dados e criar automaticamente o schema `miniautorizador`:

```bash
docker-compose up -d mysql
```

> **Nota de Configuração (`application.properties`):**
> A aplicação está configurada para se conectar ao banco no host local sem senha para o usuário `root`:
> * **URL:** `jdbc:mysql://localhost:3306/miniautorizador`
> * **Usuário:** `root`
> * **Senha:** *(vazia)*

---

### 2. Compilar e Executar a Aplicação

Com o container MySQL em execução, inicie a aplicação Spring Boot usando o Maven Wrapper:

```bash
# Limpar e compilar o projeto
./mvnw clean package -DskipTests

# Executar a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## Suíte de Testes Automatizados

A suíte de testes do projeto foi projetada para cobrir isoladamente e de forma integrada a lógica de negócios, contratos de API e resiliência transacional sob alta carga.

### Executando os Testes

Para rodar todos os testes unitários e de integração:

```bash
./mvnw test
```

### Principais Testes Incluídos:

* **`CartaoServiceTest`:** Valida a criação de cartões, verificação de duplicidade e geração do hash BCrypt.
* **`TransacaoServiceTest`:** Valida regras de débito, recusa por senha/saldo/inexistência e comparativo de hashes de senha.
* **`CartaoControllerTest` & `TransacaoControllerTest`:** Testes de integração Web via `MockMvc` com checagem de códigos de status HTTP (`201`, `200`, `400`, `404`, `422`) e autenticação HTTP Basic.
* **TransacaoConcorrenciaTest (Testes de Concorrência Paralela):** Teste unitário e de integração disparando *threads* concorrentes via `ExecutorService` e `CountDownLatch` para garantir a integridade do saldo e o bloqueio transacional `FOR UPDATE`.

## Histórico Evolutivo e Commits do Projeto

Esta seção detalha o fluxo cronológico e a maturação técnica da arquitetura do projeto durante o seu desenvolvimento:

```text
a335e4d (Commit 1) - Versionamento do projeto Mini Autorizador
  ├── Estrutura inicial do Spring Boot com Maven.
  ├── Mapeamento de Entidades JPA (Cartao, Transacao) e repositórios.
  └── Implementação primária de Controllers REST.

a2947cd (Commit 2) - Ajustes após validações e testes automatizados
  ├── Implementação da suíte inicial de testes unitários com Mockito.
  └── Testes de validação de DTOs e Bean Validation.

97e0b12 (Commit 3) - Create README.md
  └── Documentação inicial com instrução de execução básica e descrição de endpoints.

e9e3e51 (Commit 4) - Ajuste no método de criação do cartão, pós revisão da especificação, onde foi identificado divergência como o contrato.
  ├── Alinhamento do contrato do endpoint POST /cartoes com o envio da senha conforme especificação.
  └── Alteração na suíte de testes do Cartão para adequar ao ajuste do contrato.

5cd2f5c & e86495f (Commits 5 e 6) - Merge e Update README.md
  └── Atualização da documentação README.md com as alteração realizadas no e9e3e51 (Commit 4).

692937b (Commit 7) - refactor(core): aplica melhorias de segurança BCrypt e tratamento de concorrencia.
  ├── Implementa criptografia BCrypt para senhas de cartao e tratamento de DataIntegrityViolation na camada de servico.
  └── Parametriza credenciais HTTP Basic no SecurityConfig via application.properties.

1196749 (Commit 8) - refactor(test): amplia suite de testes unitarios/integracao.
  ├── Atualiza CartaoServiceTest e TransacaoServiceTest para validar BCrypt e saveAndFlush.
  ├── Adiciona TransacaoConcorrenciaTest para simulacao de transacoes simultaneas.
  └── Adequa testes de Controller ao contrato DTO e Bean Validation.

Refatoração README.md
  └── Refinamento completo do README.md com arquitetura, contratos e exemplos revisados, instruções de execução e  detalhamento da cobertura da suíte de testes.
```
