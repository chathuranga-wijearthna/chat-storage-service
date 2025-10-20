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

## Rate limiting (Bucket4j)

This service enforces rate limiting using Bucket4j at the servlet filter level. By default, limits are applied per API key (header `X-API-KEY`) and fall back to client IP if the header is absent.

- Filter: `RateLimitFilter` (ordered to run after `ApiKeyFilter`)
- Headers returned on every request (when enabled):
  - `X-RateLimit-Limit`: total allowed requests in the current window
  - `X-RateLimit-Remaining`: remaining requests in the current window
  - `Retry-After`: included when limited (seconds until next token)
- Error on limit exceeded: HTTP 429 with body `{ "code": "ERR_CS_RATE_01", "message": "Too many requests" }`

### Configuration

Configure via `application.yml` under the `ratelimit` prefix:

```
ratelimit:
  enabled: true               # turn rate limiting on/off
  capacity: 60                # max requests in the window
  refillTokens: 60            # tokens added per window
  refillPeriodSeconds: 60     # window length in seconds
  perApiKey: true             # use X-API-KEY as the limiter key; else use client IP
  includeHeaders: true        # add X-RateLimit-* headers to responses
  skipPaths:                  # paths not subject to rate limiting
    - /actuator/health
    - /v3/api-docs
    - /swagger-ui
```
