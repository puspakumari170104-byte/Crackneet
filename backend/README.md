# CrackNEET Backend

## Local setup
1. Install Node.js 20+.
2. In this folder run `npm install`.
3. Run `docker compose up -d`.
4. Copy `.env.example` to `.env` and set `DATABASE_URL`.
5. Start API: `npm start`.
6. The API creates/updates its required PostgreSQL tables on startup and automatically seeds the bundled question bank when the questions table is empty.
7. Health check: `GET /api/health`.

## Production
- Use a managed PostgreSQL database and an HTTPS API URL.
- Set `DATABASE_SSL=true` when the provider requires TLS.
- Set a specific `CORS_ORIGIN` instead of `*` for production.
- Do not commit `.env` or database credentials.
- The Android app must call the deployed HTTPS API base URL, not localhost.

## Main API
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `GET /api/users/me/progress`
- `DELETE /api/auth/account`
- `GET /api/questions`
- `POST /api/tests/submit`
- `GET /api/health`
