# Log Analyser (Spring Boot)

Initial backend logic scaffold for analyzing uploaded logs/images with AI.

## Prerequisites

- Java 17+
- Optional: OpenAI API key for real AI analysis
- Optional: Tesseract OCR installed locally (for real image text extraction)

## Configuration

Default config is in `src/main/resources/application.properties` and uses in-memory H2 DB for quick local testing.
Schema migrations are not auto-run by framework. SQL scripts are provided in `db/scripts` for manual execution.

Set OpenAI key (PowerShell):

```powershell
$env:OPENAI_API_KEY="your_api_key_here"
```

OCR configuration (`application.properties`):

```properties
app.ocr.command=tesseract
app.ocr.language=eng
app.ocr.timeout-seconds=30
```

If `tesseract` is not in PATH, set full command path, for example:

```properties
app.ocr.command=C:\\Program Files\\Tesseract-OCR\\tesseract.exe
```

## Run

If Maven is installed:

```powershell
mvn spring-boot:run
```

## Frontend (React + Vite)

Frontend is in a separate folder and runs independently from Spring Boot:

`frontend/`

Start frontend:

```powershell
cd frontend
npm install
npm run dev
```

Frontend default URL: `http://localhost:5173`

It calls backend APIs from `http://localhost:8080` by default.  
Override with env variable if needed:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

## API

`POST /api/v1/analyze` with `multipart/form-data`
- `files` (one or more)
- `text` (optional user context / issue focus)
- `sessionId` (optional, use to continue an existing chat/session)
- Response header: `X-Chat-Session-Id` (created/reused session id)

`GET /api/v1/sessions/{sessionId}`
- Load full persisted chat context and stored analysis results.

`DELETE /api/v1/sessions/{sessionId}`
- Delete session and related messages/results.

PowerShell sample:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/analyze" ^
  -F "files=@sample.log" ^
  -F "files=@sample.txt"
```

File upload with additional user context:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/analyze" ^
  -F "files=@sample.log" ^
  -F "text=Please focus on payment timeout errors after deployment."
```

Follow-up on existing session:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/analyze" ^
  -F "files=@sample.log" ^
  -F "sessionId=1" ^
  -F "text=Now focus on db connection spikes."
```

## Current flow

1. Validate input files
2. Parse text logs and image OCR via local Tesseract (OCR failures are logged and skipped)
3. Normalize logs and optional text context
4. Fetch prior chat context (if session exists) and build prompt sections: USER CONTEXT + LOG EVIDENCE
5. Send prompt to OpenAI Chat Completions API
6. Parse structured JSON into API response DTO
7. Persist user message, assistant response, and analysis metadata in DB
8. Return structured response body with `X-Chat-Session-Id` response header

## Notes

- If `OPENAI_API_KEY` is missing, app returns local fallback analysis JSON.
- Maven wrapper is not added yet.
