# RAG Chat Storage Microservice (Spring Boot)

A small Spring Boot service to store chat sessions and messages.

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker + Docker Compose (optional for containerized run)


## Environment Variables
The service reads configuration from environment variables (with sane defaults):
- `SERVER_PORT` (default: `8081`)
- `API_KEY` (default: `change-me-please`)
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `chat_db`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)


## Run with Docker Compose
1. Create a `.env.dev` file in the project root (values can be changed as needed):
   ```env
   SERVER_PORT=8081
   API_KEY=change-me-please
   DB_NAME=chat_db
   DB_USER=postgres
   DB_PASSWORD=postgres
   DB_HOST=db
   DB_PORT=5432
   ```
2. Start services:
   ```bash
   docker compose up --build
   ```
3. App will be available at `http://localhost:8081`.


## API and Docs
- Base path: `/api/v1/session`
- API Key header: `X-API-KEY: <your key>`
  - API key is required for API endpoints. It is NOT required for: `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`.
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Health: `http://localhost:8081/actuator/health`


## Postman collection
- File: `postman/Chat Storage.postman_collection.json`
- Import into Postman, then set collection Variables:
  - `baseUrl`: `http://localhost:8081`
  - `apiKey`: your API key
  - `userId`: your test user id
  - `sessionId`: leave empty initially; set after creating a session
- Requests included: create/list/rename/favorite/delete session, add/list messages.
