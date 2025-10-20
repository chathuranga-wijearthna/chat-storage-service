# RAG Chat Storage Microservice (Spring Boot)

A small Spring Boot service to store chat sessions and messages.

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker + Docker Compose (optional for containerized run)


## Environment Variables
The service reads configuration from environment variables (with sane defaults):
- `API_KEY` (default: `change-me-please`)
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `chat_db`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)


## Run with Docker Compose
1. Create a `.env.dev` file in the project root (values can be changed as needed):
   ```env
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
3. App will be available at `http://localhost:8082`.


## API and Docs
- Base path: `/api/v1/session`
- API Key header: `X-API-KEY: <your key>`
  - API key is required for API endpoints. It is NOT required for: `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`.
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- Health: `http://localhost:8082/actuator/health`


## Postman collection
- Collection: `postman/chat-storage-service.postman_collection.json`
- Environment (optional): `postman/chat-storage-service.postman_environment.json`

How to use:
1. Import both files into Postman (Collection and Environment).
2. Select the "Chat Storage Service Local" environment or set these collection variables:
   - `baseUrl`: `http://localhost:8082` (default per `src/main/resources/application.yml`)
   - `apiKey`: your API key (default `change-me-please`)
   - `userId`: your test user id (e.g., `demo-user-1`)
   - `sessionId`: leave empty; it will be captured after creating a session.
3. Run the Health request first to verify the service is up.
4. Use "Create chat session" then "Add message to session"; the collection will capture `sessionId` for you.

Requests included: create/list/rename/favorite/delete session, add/list messages, and health.
