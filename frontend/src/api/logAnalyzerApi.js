const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

async function parseError(response) {
  try {
    const data = await response.json();
    if (data && typeof data.error === "string") {
      return data.error;
    }
  } catch (_e) {
    // fall through
  }
  return `Request failed with status ${response.status}`;
}

export async function analyzeLogs(formData) {
  const response = await fetch(`${BASE_URL}/api/v1/analyze`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  const sessionHeader = response.headers.get("X-Chat-Session-Id");
  const parsedSessionId = sessionHeader ? Number(sessionHeader) : null;
  const analysis = await response.json();
  return {
    sessionId: Number.isFinite(parsedSessionId) ? parsedSessionId : null,
    analysis,
  };
}

export async function getSession(sessionId) {
  const response = await fetch(`${BASE_URL}/api/v1/sessions/${sessionId}`);
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function deleteSession(sessionId) {
  const response = await fetch(`${BASE_URL}/api/v1/sessions/${sessionId}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}
