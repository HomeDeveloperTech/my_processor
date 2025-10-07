# FicoProcessor

Aplicação Spring Boot 3.3.x construída com Java 21 e Maven.

## Requisitos

- Java 21 (JDK)
- Maven 3.9+
- Docker (opcional, apenas para executar os testes com Testcontainers)

## Build

Para compilar o projeto sem executar os testes:

```bash
mvn -q -DskipTests package
```

Para compilar incluindo testes:

```bash
mvn clean verify
```

## Execução

Execute a aplicação localmente com o comando:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

- Documentação OpenAPI/Swagger: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`

Credenciais padrão (em memória):

- Usuário: `user`
- Senha: `password`
