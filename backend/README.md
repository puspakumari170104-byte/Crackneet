# CrackNEET Backend

## Local setup
1. Install Node.js 20+ and Docker.
2. In this folder run `npm install`.
3. Run `docker compose up -d`.
4. Copy `.env.example` to `.env`.
5. Apply the schema:
   `docker exec -i crackneet-postgres psql -U postgres -d crackneet < schema.sql`
6. Start API: `npm start`
7. Health check: `GET /api/health`

The Android app should call the deployed HTTPS API base URL, not localhost, in production.
