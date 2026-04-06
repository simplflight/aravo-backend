# 🚀 Aravo API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![CI](https://img.shields.io/badge/CI-GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-Google-4285F4?style=for-the-badge&logo=google&logoColor=white)

> Motor RESTful para gerenciamento de rotinas, engine de gamificação e economia virtual.

O **Aravo API** é o serviço de backend central da plataforma Aravo. Mantido pela **Simple Flight Team**, este repositório é responsável por expor os endpoints REST, gerenciar a persistência de dados e orquestrar as regras de negócio complexas que transformam o combate à procrastinação em uma jornada gamificada.

---

## ✨ Arquitetura de Negócios e Funcionalidades Core

A arquitetura foi desenhada para garantir escalabilidade transacional, altíssima performance e segurança no processamento de atividades diárias.

* 🔐 **Autenticação e Segurança**: API protegida por **Spring Security** com autenticação stateless baseada em Tokens JWT (`SecurityFilter` e `TokenService`). Integração nativa de Social Login via **Google OAuth2**.
* 🎮 **Motor de Gamificação (`PointCalculationEngine`)**: Serviço responsável por processar o ganho de Pontos e aplicar multiplicadores baseados em regras dinâmicas (como finais de semana e campanhas).
* 🧠 **Gestão de Foco**: Ciclo de vida completo de atividades (criação, edição e exclusão) com tracking de tempo validado diretamente no servidor.
* 🛒 **Transações e Economia (`Item` & `Inventory`)**: Controle transacional rigoroso com **Pessimistic Locking** de banco de dados para prevenir fraudes e Condição de Corrida (*Double Spend*). A vitrine da loja utiliza **Cache Distribuído (Redis)** para respostas na casa dos milissegundos.
* 🔥 **Motor de Streaks (`StreakService`)**: Utiliza o padrão avançado de **Lazy Evaluation** (Avaliação Preguiçosa) para auditar lacunas no calendário e consumir itens de proteção (`STREAK_FREEZE`) apenas no momento da interação do usuário, eliminando o gargalo de processamentos pesados em background (Cron Jobs).
* 🌍 **Internacionalização (i18n)**: Suporte dinâmico a múltiplos idiomas via `LocaleConfig`, garantindo que exceções, validações e retornos da API cheguem formatados de acordo com o Locale do client.

---

## 🛠️ Stack Técnica

| Categoria          | Tecnologia        | Descrição                                                                                    |
|:-------------------|:------------------|:---------------------------------------------------------------------------------------------|
| **Linguagem**      | Java 21           | Features modernas como Virtual Threads e Pattern Matching.                                   |
| **Framework**      | Spring Boot 3.x   | Web, Security, Data JPA, Validation, Cache.                                                  |
| **Banco de Dados** | PostgreSQL        | Armazenamento relacional robusto.                                                            |
| **Cache**          | Redis             | Banco de dados em memória para cache distribuído.                                            |
| **Migrations**     | Flyway            | Controle de versão do schema automatizado.                                                   |
| **Infraestrutura** | Docker & Compose  | Containerização isolada para DB, Redis e Aplicação.                                          |
| **CI / Automação** | GitHub Actions    | Pipeline automatizado de build e testes unitários (AAA) com injeção de serviços de DB/Cache. |
| **Documentação**   | OpenAPI / Swagger | Contrato interativo e tagueado dos endpoints REST.                                           |

---

## ⚙️ Configuração e Variáveis de Ambiente

A aplicação adota o princípio de *Fail-Fast* em Produção e facilidade em Desenvolvimento através de **Spring Profiles** (`application-dev` e `application-prod`).

Principais variáveis de ambiente injetadas via Docker Compose:
* `SPRING_PROFILES_ACTIVE`: Define o profile ativo (default: `dev`).
* `DB_URL`, `DB_USER`, `DB_PASSWORD`: Credenciais do PostgreSQL.
* `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`: Conexão com o servidor de Cache.
* `spring.config.import`: Injeção de credenciais sensíveis (OAuth2) ignoradas no versionamento.

---

## 🚀 Guia de Instalação e Execução

### Pré-requisitos
* Git
* Java 21
* Docker & Docker Compose

### 1. Clonando o Repositório
```bash
git clone https://github.com/simplflight/aravo-backend.git
cd aravo-backend
```

### 2. Configuração de Variáveis Sensíveis (Local)
O projeto utiliza OAuth2 e requer credenciais do Google para rodar localmente. Antes de subir a aplicação:
1. Copie o arquivo de template de segredos:
```bash
cp src/main/resources/application-secrets.properties.example src/main/resources/application-secrets.properties
```
2. Abra o arquivo gerado e preencha com o seu Client ID e Client Secret gerados no Google Cloud Console.

### 3. Subindo a Infraestrutura Completa (Docker)
O `docker-compose.yml` orquestra o PostgreSQL, o Redis e a API. Para construir a imagem e iniciar os serviços:
```bash
docker compose up -d --build
```
(O Docker utilizará healthchecks para garantir a ordem correta de inicialização. As migrations do Flyway rodarão no startup).

### 4. Execução Local para Desenvolvimento
Para debugar localmente, suba apenas a infraestrutura de dados:
```bash
docker compose up -d db redis
```
E inicie a aplicação pelo Maven Wrapper:
```bash
./mvnw spring-boot:run
```

### 5. Acessando a Documentação (Swagger UI)
Com a aplicação rodando (porta `8080`), acesse o Swagger UI para visualizar o contrato da API e testar os endpoints:

👉 http://localhost:8080/swagger-ui/index.html

---

**Padrões Adotados**: Arquitetura em Camadas (Controller, Service, Repository), **Princípio de Responsabilidade Única (SRP)**, Testes Unitários baseados em **AAA** e **Conventional Commits** para o histórico do Git. 

> Desenvolvido com ☕ e 💻 pela Simple Flight Team.