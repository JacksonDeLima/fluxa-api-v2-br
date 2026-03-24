# 🚀 TaskFlow API

API RESTful para gerenciamento de usuários e tarefas com autenticação JWT e controle de acesso.

---

## 🧠 Tecnologias

- Java 17+
- Spring Boot
- Spring Security
- JWT (JSON Web Token)
- JPA / Hibernate
- PostgreSQL
- Docker

---

## 🔐 Funcionalidades

### Autenticação
- Registro de usuário
- Login com JWT
- Senha criptografada (BCrypt)

### Usuários
- Controle por roles (USER / ADMIN)

### Tarefas
- Criar tarefa
- Listar tarefas do usuário autenticado
- Buscar tarefa por ID
- Atualizar tarefa
- Deletar tarefa

---

## 🔑 Segurança

- Autenticação via JWT
- Endpoints protegidos
- Cada usuário acessa apenas suas próprias tarefas

---

## 📦 Endpoints

### Auth

```http
POST /auth/register
POST /auth/login
````

### Tasks

```http
GET    /tasks
GET    /tasks/{id}
POST   /tasks
PUT    /tasks/{id}
DELETE /tasks/{id}
```

---

## ▶️ Como rodar o projeto

### 1. Subir banco com Docker

```bash
docker compose up -d
```

### 2. Rodar aplicação

```bash
./mvnw spring-boot:run
```

---

## 🧪 Testes

Use Postman ou Insomnia.

1. Registrar usuário
2. Fazer login
3. Copiar token JWT
4. Usar Bearer Token nas requisições

---

## 📌 Autor

Desenvolvido por Jackson De Lima

  
