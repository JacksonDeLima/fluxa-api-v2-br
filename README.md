# TaskFlow API

![Java](https://img.shields.io/badge/Java-17-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-success?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue?logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-black?logo=json-web-tokens)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black)

Uma API REST robusta desenvolvida com **Java** e **Spring Boot** para o gerenciamento de tarefas e usuários. O sistema conta com autenticação segura via **JWT**, controle de acesso baseado em perfis (Roles) e persistência de dados utilizando **PostgreSQL**.

##  Funcionalidades

- **Autenticação e Autorização**: Registro e login de usuários com geração de tokens JWT de forma *stateless*.
- **Controle de Acesso**: Proteção de rotas baseada em perfis de acesso (`ADMIN` e `USER`).
- **Gestão de Tarefas (CRUD)**: Criação, listagem, atualização e exclusão de tarefas.
- **Isolamento de Dados**: Usuários comuns só têm acesso às suas próprias tarefas.
- **Tratamento de Exceções**: Padronização global de erros para respostas consistentes (usando `@RestControllerAdvice`).
- **Documentação Interativa**: Interface do Swagger (OpenAPI) disponível para testes e visualização dos endpoints.

##  Tecnologias Utilizadas

- **Linguagem**: Java 17
- **Framework**: Spring Boot (Web, Data JPA, Security, Validation)
- **Segurança**: Spring Security + JWT (Bcrypt para senhas)
- **Banco de Dados**: PostgreSQL
- **Migrações / ORM**: Hibernate / JPA
- **Documentação**: Swagger (SpringDoc OpenAPI)
- **Utilitários**: Lombok, Docker Compose

##  Como executar localmente

### Pré-requisitos
- Java 17+
- Maven
- Docker e Docker Compose

### 1. Suba o banco de dados
```bash
docker compose up -d
```

### 2. Execute a aplicação
```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

##  Documentação da API (Swagger)

Com a aplicação em execução, acesse a interface do Swagger para explorar e testar os endpoints:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

##  Endpoints Principais

* **Auth**: `/auth/register` | `/auth/login`
* **Tarefas**: `/tasks` (GET, POST) | `/tasks/{id}` (GET, PUT, DELETE)
* **Admin**: `/admin/**` *(Acesso restrito à role ADMIN)*

---

Desenvolvido por **Jackson De lima**
