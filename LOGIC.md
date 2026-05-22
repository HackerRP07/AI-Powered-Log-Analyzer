# LogAnalyser Current Logic

## 1) API Endpoints

- `GET /api/v1`
  - Basic service status/info.
- `POST /api/v1/analyze` (`multipart/form-data`)
  - `files` (required): `txt`, `log`, `png`, `jpg`, `jpeg`
  - `text` (optional): user context / issue focus
  - `sessionId` (optional): append follow-up to existing session
  - response header `X-Chat-Session-Id`: created/reused session id
- `GET /api/v1/sessions/{sessionId}`
  - Reload persisted session context + messages + stored analysis results
- `DELETE /api/v1/sessions/{sessionId}`
  - Delete session and related rows

## 2) End-to-End Flow

1. Request hits `AnalysisController`.
2. `AnalysisOrchestratorService` receives input.
3. For `/analyze` with multipart:
   - Validate files (`FileValidationService`).
   - Parse files (`LogParserService`):
     - Text/log files -> `TextLogParser`
     - Images -> `ImageTextExtractorService` (Tesseract OCR)
4. Normalize LOG EVIDENCE and optional USER CONTEXT (`NormalizationService`):
   - Trims input
   - Redacts sensitive tokens via `RedactionUtil`:
     - `password=...` -> `password=***`
     - `token=...` -> `token=***`
5. AI stage (`AiAnalysisService`):
   - Build strict JSON prompt (`PromptBuilder`) with two clear sections:
     - `USER CONTEXT`
     - `LOG EVIDENCE`
   - USER CONTEXT is issue focus, not evidence.
   - Prior chat history is included for follow-up continuity.
   - Call model (`OpenAiClient`)
6. Format response (`ReportFormatterService`):
   - Parse AI JSON into:
     - `summary`
     - `findings[]`
     - `timeline[]`
     - `recommendations[]`
     - `confidenceScore`
7. Persist chat/session data (`ChatSessionService`):
   - USER message is stored
   - ASSISTANT message is stored
   - Structured analysis result is stored
8. Return final `AnalysisResponseDto`.

## 3) OCR Logic (ImageTextExtractorService)

- Uses local Tesseract CLI.
- Configurable properties:
  - `app.ocr.command`
  - `app.ocr.language`
  - `app.ocr.timeout-seconds`
- Behavior:
  - `LogParserService` always calls OCR for image files.
  - If timeout/error/not installed -> logs warning and returns empty text for that image
  - If successful -> extracted text from image

## 4) OpenAI Call Behavior

- If `OPENAI_API_KEY` exists:
  - Calls `/v1/chat/completions`
  - Reads `choices[0].message.content`
- If missing:
  - Returns local fallback JSON response (no crash)

## 5) Error Handling

- `GlobalExceptionHandler` returns clean error JSON:
  - `400` for invalid input/validation
  - `500` for server/processing issues

## 6) Persistence Model

- `chat_session`
  - One row per conversation.
- `chat_message`
  - USER/ASSISTANT chat entries linked to session.
- `chat_analysis_result`
  - Stored analysis output:
    - summary
    - findings JSON
    - timeline JSON
    - recommendations JSON
    - confidence score
    - raw AI response

Deleting a `chat_session` removes related `chat_message` and `chat_analysis_result` rows via cascade rules.
