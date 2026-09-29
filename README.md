# farm_app
App para gerenciamento fazendário(gado, alimentação e leite) 

## Docker: projeto completo em dois comandos

Requisitos: Docker com Docker Compose. Execute na raiz do repositório, nesta ordem:

```sh
docker compose -f compose.backend.yml up -d --build --wait
docker compose -f compose.frontend.yml up -d --build --wait
```

Isso cria três containers: backend Java 21, servidor web Nginx com o frontend React
e PostgreSQL 16. Os builds acontecem dentro do Docker; Java, Maven e Node locais
não são necessários. Os mesmos comandos recompilam e atualizam cada parte.

- Frontend: http://localhost:5173
- API: http://localhost:8081
- Swagger: http://localhost:8081/swagger-ui/index.html
- Login inicial em banco vazio: admin@farmapp.com / admin123

O backend cria a rede compartilhada `farm-app-web`; por isso ele deve subir antes
do frontend na primeira execução. O Nginx encaminha `/api/` ao backend, removendo
esse prefixo. Rotas do React também funcionam ao recarregar a página.

O PostgreSQL usa um volume persistente e não publica portas no host. Este banco é
novo e independente de qualquer PostgreSQL já instalado. O Flyway aplica as
migrações existentes e o Hibernate valida o schema, sem recriar os dados.
A API usa a porta 8081 para não conflitar com serviços existentes na porta 8080.

### Configuração opcional

Os padrões são para uso local e as portas publicadas ficam restritas a localhost.
Para sobrescrever valores, crie um `.env` na raiz do repositório (ignorado pelo Git)
ou exporte as variáveis antes de executar os comandos. O `.env` de
`backend/farmapp` não é carregado por estes arquivos Compose.

- `BACKEND_PORT` (8081), `FRONTEND_PORT` (5173).
- `APP_FRONTEND_URL` (http://localhost:5173): ajuste também ao mudar a porta web.
- `POSTGRES_PASSWORD`, `APP_SECURITY_JWT_SECRET` (pelo menos 32 bytes),
  `ADMIN_EMAIL`, `ADMIN_PASSWORD`: substitua os valores locais antes de qualquer
  implantação compartilhada. As credenciais iniciais do banco/admin só são
  aplicadas na inicialização; mudar variáveis não altera senhas já persistidas.
- Email fica desabilitado por padrão. Para enviar confirmações, configure
  `APP_EMAIL_ENABLED=true`, `BREVO_SMTP_USERNAME`, `BREVO_SMTP_PASSWORD` e
  `APP_MAIL_FROM`.
- Stripe é opcional: `STRIPE_PUBLIC_KEY`, `STRIPE_SECRET_KEY`,
  `STRIPE_WEBHOOK_SECRET`, `STRIPE_PREMIUM_PRICE_ID`, `STRIPE_SUCCESS_URL` e
  `STRIPE_CANCEL_URL`.

### Logs e parada

```sh
docker compose -f compose.backend.yml logs -f
docker compose -f compose.frontend.yml logs -f
```

Para remover os containers, pare primeiro o frontend, que usa a rede do backend:

```sh
docker compose -f compose.frontend.yml down
docker compose -f compose.backend.yml down
```

O volume do PostgreSQL é preservado. Adicionar `-v` ao `down` do backend apaga
permanentemente os dados desse banco.
