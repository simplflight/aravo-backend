# 🚀 Aravo API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

> Motor RESTful para gerenciamento de rotinas, engine de gamificação e economia virtual.

O **Aravo API** é o serviço de backend central da plataforma Aravo. Mantido pela **Simple Flight Team**, este repositório é responsável por expor os endpoints REST, gerenciar a persistência de dados e orquestrar as regras de negócio complexas que transformam o combate à procrastinação em uma jornada gamificada.

---

## ✨ Arquitetura de Negócios e Funcionalidades Core

A arquitetura foi desenhada para garantir escalabilidade transacional e segurança no processamento de atividades diárias.

* 🔐 **Autenticação e Segurança**: API protegida por **Spring Security** com autenticação stateless baseada em Tokens JWT (`SecurityFilter` e `TokenService`). Endpoints de registro e login são abertos, enquanto rotas de rotina e economia exigem token válido.
* 🎮 **Motor de Gamificação (`PointCalculationEngine`)**: Serviço isolado responsável por processar o ganho de Experiência (XP), cálculo de níveis e atribuição de moedas virtuais após a conclusão de atividades.
* 🛒 **Transações e Economia (`Item` & `Inventory`)**: Controle transacional rigoroso para garantir a consistência na compra de itens da loja e no armazenamento seguro no inventário do usuário.
* 🔥 **Motor de Streaks (`UserDailyTracking`)**: Processamento em background e validação de consistência diária, calculando multiplicadores de recompensas para engajamento contínuo.
* 🌍 **Internacionalização (i18n)**: Suporte dinâmico a múltiplos idiomas via `LocaleConfig`, garantindo que exceções, validações e retornos da API cheguem formatados de acordo com o Locale do client.

---

## 🛠️ Stack Técnica

| Categoria          | Tecnologia        | Descrição                                                  |
|:-------------------|:------------------|:-----------------------------------------------------------|
| **Linguagem**      | Java 21           | Features modernas como Virtual Threads e Pattern Matching. |
| **Framework**      | Spring Boot 3.x   | Web, Security, Data JPA, Validation.                       |
| **Banco de Dados** | PostgreSQL        | Armazenamento relacional robusto.                          |
| **Migrations**     | Flyway            | Controle de versão do schema automatizado.                 |
| **Infraestrutura** | Docker & Compose  | Containerização isolada para DB e Aplicação.               |
| **Documentação**   | OpenAPI / Swagger | Contrato interativo dos endpoints REST.                    |

---

## ⚙️ Configuração e Variáveis de Ambiente

O comportamento da API pode ser customizado através de variáveis de ambiente. No ambiente de desenvolvimento via Docker Compose, as seguintes variáveis são injetadas automaticamente:

* `SPRING_PROFILES_ACTIVE`: Define o profile ativo (ex: `dev`, `prod`).
* `DB_URL`: String de conexão JDBC (ex: `jdbc:postgresql://db:5432/aravo_db`).
* `DB_USER` / `DB_PASSWORD`: Credenciais do banco de dados.

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

### 2. Subindo a Infraestrutura e a Aplicação (Docker)
O `docker-compose.yml` orquestra o banco de dados PostgreSQL e a API. Para construir a imagem e iniciar os serviços:
```bash
docker-compose up -d --build
```
(O Docker utilizará um healthcheck para garantir que a API inicie apenas quando o PostgreSQL estiver aceitando conexões. As migrations do Flyway rodarão no startup).

### 3. Execução Local para Desenvolvimento
Para debugar localmente, suba apenas o banco:
```bash
docker-compose up -d db
```
E inicie a aplicação pelo Maven Wrapper:
```bash
./mvnw spring-boot:run
```

### 4. Acessando a Documentação (Swagger UI)
Com a aplicação rodando (porta `8080`), acesse o Swagger UI para visualizar o contrato da API e testar os endpoints:

👉 http://localhost:8080/swagger-ui/index.html

---

**Padrões Adotados**: Arquitetura em Camadas (Controller, Service, Repository) e **Conventional Commits** para o histórico do Git. 

> Desenvolvido com ☕ e 💻 pela Simple Flight Team.