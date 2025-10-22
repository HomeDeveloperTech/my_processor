# Fico Processor

Projeto Spring Boot 3.x com Java 17 e Maven.

## Comandos úteis

- `mvn -Pdev spring-boot:run`
- `mvn -Pprod package`
- `mvn test`

## Execução via JAR (Linux/Ubuntu/Windows)

Fornecemos scripts para iniciar o projeto carregando variáveis de ambiente a partir de um arquivo `.env` na raiz do projeto, com suporte a seleção de profile do Spring.

1. Copie o arquivo de exemplo:
   - `cp .env.example .env` (Linux/Ubuntu) ou crie um `.env` equivalente no Windows.
   - Edite `.env` conforme seu ambiente (perfil, porta, datasource, etc.).
2. Dê permissão de execução (se necessário):
   - Linux/Ubuntu: `chmod +x scripts/start-linux.sh scripts/start-ubuntu.sh`
3. Inicie a aplicação (escolha um dos scripts):
   - Linux genérico: `./scripts/start-linux.sh`
   - Ubuntu: `./scripts/start-ubuntu.sh`
   - Windows (PowerShell): `powershell -ExecutionPolicy Bypass -File scripts\start-windows.ps1`
   - Windows (CMD .bat): `scripts\start-windows.bat`

### Selecionando o profile do Spring

Você pode escolher o profile de três formas (precedência: CLI > SPRING_PROFILES_ACTIVE > PROFILE):
- Por parâmetro de linha de comando:
  - Linux/Ubuntu: `./scripts/start-linux.sh --profile dev`
  - Ubuntu: `./scripts/start-ubuntu.sh -p prod`
  - Windows: `powershell -File scripts\start-windows.ps1 -Profile dev`
  - Windows (CMD .bat): `scripts\start-windows.bat -profile dev`
- Definindo a variável `SPRING_PROFILES_ACTIVE` no `.env` ou no ambiente.
- Usando o alias `PROFILE` no `.env` (caso `SPRING_PROFILES_ACTIVE` não esteja definido).

Os scripts passam o profile selecionado para o Spring Boot via argumento `--spring.profiles.active`.

### Outras opções

- Os scripts tentam localizar o JAR em `target/`. Se não encontrarem, executam `mvn -DskipTests package` automaticamente.
- Você pode definir `JAVA_OPTS` no `.env` (ex.: `-Xms256m -Xmx512m`). No Windows, também é possível usar `-JavaOpts` no comando.
- Para usar outro arquivo de variáveis, defina `ENV_FILE` antes de executar:
  - Linux/Ubuntu: `ENV_FILE=.env.prod ./scripts/start-linux.sh`
  - Windows: `powershell -File scripts\start-windows.ps1 -EnvFile .env.prod`
  - Windows (CMD .bat): `scripts\start-windows.bat -EnvFile .env.prod`
- Para especificar o JAR manualmente:
  - Linux/Ubuntu: `JAR_FILE=target/meu.jar ./scripts/start-linux.sh`
  - Windows: `powershell -File scripts\start-windows.ps1 -JarFile target\meu.jar`
  - Windows (CMD .bat): `scripts\start-windows.bat -JarFile target\meu.jar`
