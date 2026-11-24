# Integração Spring Boot 4 com Keycloak (Java 25)

Projeto de estudo que demonstra como uma API REST desenvolvida com Spring Boot 4 usa o Keycloak como servidor de autorização. O Spring Security valida o token JWT enviado pelo cliente, converte os roles do client Keycloak em autoridades do Spring e aplica regras de acesso aos endpoints da aplicação.

## Tecnologias

- Java 25
- Spring Boot 4.0.2
- Spring Security e OAuth2 Resource Server
- Maven
- Keycloak 26.5.2
- Docker Compose

## Arquitetura

```text
Cliente HTTP
    |
    | Authorization: Bearer <JWT>
    v
API Spring Boot (porta 8080)
    |
    | JWT issuer: http://localhost:28080/realms/sboot-keycloak
    v
Keycloak (porta 28080)
```

O fluxo de acesso é:

1. O cliente obtém um JWT no Keycloak.
2. O cliente envia o JWT no header `Authorization`.
3. O Spring valida a assinatura e o issuer do token.
4. `KeycloakResourceConverter` lê o claim `resource_access`.
5. Os roles do client `cursos-api` são convertidos para autoridades no formato `ROLE_<nome-do-role>`.
6. `SecurityConfig` decide quais endpoints podem ser acessados.

## Estrutura principal

| Caminho | Responsabilidade |
| --- | --- |
| `src/main/java/.../CursoController.java` | Expõe os endpoints REST de cursos |
| `src/main/java/.../config/SecurityConfig.java` | Configura autenticação e autorização |
| `src/main/java/.../config/KeycloakResourceConverter.java` | Converte roles de client do Keycloak em roles do Spring |
| `src/main/resources/application.yaml` | Configura o issuer do realm usado pela API |
| `docker-compose.yml` | Inicia o Keycloak em modo de desenvolvimento |

## Endpoints

| Método | Endpoint | Acesso |
| --- | --- | --- |
| `GET` | `/cursos/java` | Requer o role `visualizar-curso-java` |
| `GET` | `/cursos/spring-boot` | Requer autenticação; o role configurado no projeto é `visualizar-curso-sping-boot` |
| `GET` | `/cursos/abertos` | Público |
| `GET` | `/cursos/exclusivos` | Requer um usuário autenticado |

> Observação: o endpoint protegido `/cursos/spring-boot` usa a grafia de role `visualizar-curso-sping-boot`, mantida no projeto atual.

## Pré-requisitos

- JDK 25
- Docker com Docker Compose
- Maven 3.9+ ou o Maven Wrapper incluído no projeto

Verifique as versões locais:

```bash
java -version
docker compose version
mvn -version
```

## Execução local

### 1. Inicie o Keycloak

Na raiz do projeto, execute:

```bash
docker compose up -d
```

O console administrativo ficará disponível em `http://localhost:28080`. As credenciais definidas pelo `docker-compose.yml` são:

- Usuário: `admin`
- Senha: `admin`

Para acompanhar os logs:

```bash
docker compose logs -f keycloak
```

### 2. Configure o realm

No console administrativo do Keycloak:

1. Crie um realm chamado `sboot-keycloak`.
2. Em **Clients**, crie um client com o client ID `cursos-api`.
3. Configure o client como público, habilite **Direct access grants** e salve.
4. Em **Clients > cursos-api > Roles**, crie os roles:
   - `visualizar-curso-java`
   - `visualizar-curso-sping-boot`
5. Em **Users**, crie um usuário, defina uma senha permanente em **Credentials** e atribua os roles desejados em **Role mapping**.

Os roles devem ser criados e atribuídos como **roles do client** `cursos-api`, pois `KeycloakResourceConverter` procura o claim `resource_access.cursos-api.roles`.

### 3. Inicie a aplicação

Com o Maven instalado:

```bash
mvn spring-boot:run
```

Ou, no Linux, habilite e use o Maven Wrapper:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### 4. Obtenha um token

Substitua `USUARIO` e `SENHA` pelas credenciais cadastradas no Keycloak:

```bash
curl --request POST \
  --url "http://localhost:28080/realms/sboot-keycloak/protocol/openid-connect/token" \
  --header "Content-Type: application/x-www-form-urlencoded" \
  --data "client_id=cursos-api" \
  --data "grant_type=password" \
  --data "username=USUARIO" \
  --data "password=SENHA"
```

Copie o valor de `access_token` da resposta e exporte-o no terminal:

```bash
export ACCESS_TOKEN="SEU_ACCESS_TOKEN"
```

### 5. Teste os endpoints

Endpoint público:

```bash
curl "http://localhost:8080/cursos/abertos"
```

Endpoint protegido para usuário com o role `visualizar-curso-java`:

```bash
curl --header "Authorization: Bearer $ACCESS_TOKEN" \
  "http://localhost:8080/cursos/java"
```

Endpoint que exige apenas autenticação:

```bash
curl --header "Authorization: Bearer $ACCESS_TOKEN" \
  "http://localhost:8080/cursos/exclusivos"
```

## Executando os testes

```bash
mvn test
```

Para compilar o projeto sem executar os testes:

```bash
mvn -DskipTests compile
```

## Encerrando o ambiente

```bash
docker compose down
```
