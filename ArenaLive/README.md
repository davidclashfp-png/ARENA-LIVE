# ArenaLive — Android + Cloudflare

Aplicativo Android nativo para acompanhar partidas, resultados, estatísticas, competições e atletas. Projeto inicial com identidade visual própria, inspirado na categoria de aplicativos esportivos, sem copiar marca, logotipos ou elementos proprietários do Sofascore.

> **Estado desta versão: protótipo funcional de interface (0.1.0).** As telas usam dados fictícios identificados como demonstrativos. O backend Cloudflare tem rotas e banco D1 preparados. A integração opcional com API-Football entrega placares de futebol ao vivo, mas ainda não substitui uma fonte de dados licenciada e abrangente para todos os esportes, eventos, escalações, mapas de calor e notas.

## O que já existe

### Aplicativo Android
- Projeto nativo Kotlin + Jetpack Compose. Não é site, PWA ou WebView.
- Navegação inferior: Início, Jogos, Explorar e Favoritos.
- Cards de placar para futebol, basquete e tênis, com filtros por esporte.
- Tela de partida com resumo, linha do tempo, estatísticas e escalação demonstrativa.
- Classificação de campeonato e comparação ilustrativa entre atletas.
- Tema escuro, contraste alto e cor de destaque verde-lima.
- Modo demonstrativo que funciona sem chave de API.
- Conector opcional para buscar partidas no Worker Cloudflare.

### Backend Cloudflare
- Worker com rotas REST.
- Banco Cloudflare D1 e migração SQL.
- Rotas para partidas ao vivo, calendário, detalhes, eventos, estatísticas, classificação e comparação de atletas.
- Adaptador opcional de placares ao vivo da API-Football, com a chave guardada como secret no servidor.
- Respostas CORS para o app Android.

### GitHub
- Workflow para compilar o APK de debug e publicá-lo como artefato da execução.
- Workflow opcional para publicar o Worker Cloudflare usando secrets do GitHub.

## Requisitos

- Android Studio recente com Android SDK 36 e JDK 17.
- Uma conta GitHub para versionamento.
- Uma conta Cloudflare para o backend (o plano gratuito é suficiente para começar, respeitados os limites).
- Opcional: uma chave de provedor de dados esportivos. Não coloque chaves secretas dentro do aplicativo Android nem em arquivos enviados ao GitHub.

## 1. Abrir e testar o Android

1. Extraia o ZIP.
2. (Recomendado na primeira utilização) Abra um terminal na pasta `ArenaLive` e execute `./gradlew wrapper` (Windows: `gradlew.bat wrapper`). O script incluído baixa o Gradle 8.13 e o comando gera os arquivos oficiais do Gradle Wrapper para o Android Studio.
3. Abra o Android Studio e escolha **Open**.
4. Selecione a pasta `ArenaLive` (a pasta que contém `settings.gradle.kts`).
5. Aceite a sincronização do Gradle e instale o Android SDK 36 quando solicitado.
6. Inicie um emulador Android ou conecte um aparelho Android com depuração USB habilitada.
7. Pressione **Run** para instalar o app.

O app abre sem conta externa e sem chave: os placares e as estatísticas serão demonstrativos. Eles não representam jogos reais.

### Gerar APK localmente

No terminal, dentro da pasta `ArenaLive`:

```bash
./gradlew assembleDebug
```

No Windows:

```bat
gradlew.bat assembleDebug
```

O primeiro uso dos scripts baixa Gradle 8.13. O APK de debug ficará em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para gerar pelo Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

## 2. Criar o backend Cloudflare D1 + Worker

Abra um terminal na pasta `ArenaLive/cloudflare`.

### 2.1 Instalar as ferramentas

Instale Node.js LTS e depois execute:

```bash
npm install
npx wrangler login
```

### 2.2 Criar o banco D1

```bash
npx wrangler d1 create arenalive-db
```

O comando retorna um `database_id`. Abra `wrangler.toml` e substitua:

```toml
database_id = "PREENCHA_COM_O_ID_CRIADO_NO_CLOUDFLARE"
```

pelo ID retornado.

### 2.3 Criar as tabelas e os dados de demonstração

```bash
npx wrangler d1 migrations apply arenalive-db --remote
npx wrangler d1 execute arenalive-db --remote --file=./data_demo.sql
```

O arquivo de demonstração contém equipes e partidas fictícias para verificar as rotas. Não use esses dados como resultados reais.

### 2.4 Publicar o Worker

```bash
npx wrangler deploy
```

O Cloudflare exibirá um endereço parecido com:

```text
https://arenalive-api.SEUSUBDOMINIO.workers.dev
```

Teste no navegador:

```text
https://SEU-WORKER/api/health
https://SEU-WORKER/api/matches/live
```

### Rotas disponíveis

| Método | Rota | Objetivo |
|---|---|---|
| GET | `/api/health` | Verificar se a API está ativa |
| GET | `/api/matches/live` | Partidas marcadas como ao vivo no D1 |
| GET | `/api/matches?date=2026-11-20` | Partidas por data |
| GET | `/api/matches/:id` | Detalhes, eventos, estatísticas e jogadores |
| GET | `/api/standings/:competitionId` | Classificação de uma competição |
| GET | `/api/players/compare?ids=atleta1,atleta2` | Comparar dois atletas cadastrados |
| GET | `/api/providers/api-football/live` | Placares ao vivo da API-Football, quando configurada |

## 3. Conectar o app ao Worker

No arquivo `gradle.properties` da raiz, acrescente (ou ajuste) as propriedades abaixo:

```properties
API_BASE_URL=https://SEU-WORKER.SEUSUBDOMINIO.workers.dev
API_LIVE_PATH=/api/matches/live
```

Sincronize o Gradle e execute novamente o app.

Por padrão, o app consulta `/api/matches/live` (dados que estão no D1). Para consultar a rota do adaptador API-Football, use:

```properties
API_LIVE_PATH=/api/providers/api-football/live
```

O app só troca os cards demonstrativos por dados remotos se a API responder com partidas. Se a URL estiver vazia ou a consulta falhar, continua no modo demonstrativo.

## 4. Configurar uma fonte de placares reais (opcional)

A rota `/api/providers/api-football/live` é um adaptador inicial. Para ativá-la, configure a chave como secret no Cloudflare, dentro da pasta `cloudflare`:

```bash
npx wrangler secret put API_FOOTBALL_KEY
```

Cole a chave quando o terminal solicitar. A chave fica no Worker e não é enviada no APK.

**Atenção às limitações:** o plano gratuito publicado pelo API-Football anuncia 100 requisições por dia. Isso é insuficiente para prometer cobertura extensa e atualizações constantes para uma base grande de usuários. A rota tem cache de 60 segundos, mas ainda é necessário acompanhar o consumo. Confira limites, disponibilidade por campeonato, atribuição, licença e direitos de redistribuição diretamente com o provedor antes de publicar.

A primeira rota externa deste protótipo cobre placares de futebol. Estatísticas detalhadas, eventos, escalações, mapas de calor e avaliações avançadas dependem de endpoints e permissões de dados adicionais. Basquete, tênis e outros esportes precisam de fontes compatíveis. Não há promessa de que uma única API gratuita forneça todos esses dados para uso público/comercial.

## 5. Enviar o projeto para o GitHub

Crie um repositório vazio no GitHub. Selecione público se quiser aproveitar a execução gratuita dos workflows sem consumir minutos de Actions de um repositório privado.

Dentro da pasta `ArenaLive`:

```bash
git init
git add .
git commit -m "Versao inicial do ArenaLive"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/SEU-REPOSITORIO.git
git push -u origin main
```

Depois de enviar:
- Abra a aba **Actions** para acompanhar a compilação Android.
- Quando a compilação terminar, abra a execução e baixe o artefato `arenalive-debug-apk`.
- O workflow Android compila o APK de debug; ele não gera uma versão assinada para distribuição pública.

### Publicação automática do Worker pelo GitHub (opcional)

1. No Cloudflare, crie um API Token com permissões restritas para publicar Workers e acessar o D1 necessário.
2. No GitHub, abra **Settings → Secrets and variables → Actions**.
3. Crie o secret `CLOUDFLARE_API_TOKEN`.
4. Confirme que `cloudflare/wrangler.toml` contém o ID real do banco.
5. Faça push de uma alteração dentro de `cloudflare/`. O workflow tentará publicar o Worker.

Não coloque tokens, senhas ou chaves no código, no README ou em arquivos commitados.

## 6. Próximas etapas recomendadas

1. Escolher uma fonte de dados esportivos com cobertura e licença adequada para o uso pretendido.
2. Criar um processo de sincronização entre o provedor e D1, com cache e controle de limites.
3. Completar as telas de detalhes com eventos, estatísticas e escalações reais.
4. Implementar autenticação, perfis, notificações e sincronização de favoritos.
5. Construir um algoritmo transparente de nota de atleta, validado contra dados reais e documentado.
6. Adicionar fontes e modelos de dados para basquete, tênis e demais modalidades.
7. Criar testes, política de privacidade, ícone, capturas de tela e versão assinada de produção.

## Custos e publicação

O código, o GitHub e os planos gratuitos do Cloudflare permitem começar sem pagar hospedagem, desde que o uso fique dentro dos limites. O provedor de estatísticas pode impor limites ou cobrar por mais volume e cobertura. A publicação na Google Play tem taxa de cadastro e requisitos próprios; consulte as regras oficiais antes de planejar o lançamento.

## Estrutura

```text
ArenaLive/
├── app/                         # Aplicativo Android Kotlin/Compose
├── cloudflare/
│   ├── migrations/              # Esquema D1
│   ├── src/                     # Worker REST
│   ├── data_demo.sql            # Dados fictícios
│   └── wrangler.toml            # Configuração Cloudflare
├── .github/workflows/           # Compilação APK e deploy Worker
├── docs/                        # Notas e roadmap
├── gradlew                      # Bootstrap Gradle para macOS/Linux
├── gradlew.bat                  # Bootstrap Gradle para Windows
└── README.md
```

---

**ArenaLive 0.1.0 — projeto inicial para desenvolvimento.** Ainda não é um produto pronto para distribuição: os resultados e dados de atletas da interface são demonstrativos até a conexão e validação de provedores reais.
