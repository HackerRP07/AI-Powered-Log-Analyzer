const SESSION_IDS_KEY = "log_analyser_session_ids";
const ACTIVE_SESSION_KEY = "log_analyser_active_session_id";

export function readSessionIds() {
  try {
    const raw = localStorage.getItem(SESSION_IDS_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed
      .map((value) => Number(value))
      .filter((value) => Number.isFinite(value));
  } catch (_e) {
    return [];
  }
}

export function writeSessionIds(sessionIds) {
  const unique = [...new Set(sessionIds)].filter((value) => Number.isFinite(value));
  localStorage.setItem(SESSION_IDS_KEY, JSON.stringify(unique));
}

export function readActiveSessionId() {
  const value = Number(localStorage.getItem(ACTIVE_SESSION_KEY));
  return Number.isFinite(value) ? value : null;
}

export function writeActiveSessionId(sessionId) {
  if (sessionId == null) {
    localStorage.removeItem(ACTIVE_SESSION_KEY);
    return;
  }
  localStorage.setItem(ACTIVE_SESSION_KEY, String(sessionId));
}
