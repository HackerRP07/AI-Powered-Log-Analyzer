import { useCallback, useEffect, useMemo, useState } from "react";
import { analyzeLogs, deleteSession, getSession } from "./api/logAnalyzerApi";
import ChatHeader from "./components/ChatHeader";
import ChatWindow from "./components/ChatWindow";
import Sidebar from "./components/Sidebar";
import UploadComposer from "./components/UploadComposer";
import { sessionToChatItems, sessionToSidebarEntry } from "./utils/chatMapper";
import {
  readActiveSessionId,
  readSessionIds,
  writeActiveSessionId,
  writeSessionIds,
} from "./utils/sessionStorage";

function toErrorMessage(error) {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Something went wrong while contacting the server.";
}

function sortByUpdatedAtDesc(left, right) {
  const l = new Date(left.updatedAt || 0).getTime();
  const r = new Date(right.updatedAt || 0).getTime();
  return r - l;
}

export default function App() {
  const [sessionIds, setSessionIds] = useState([]);
  const [sessionsById, setSessionsById] = useState({});
  const [activeSessionId, setActiveSessionId] = useState(null);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [chatLoading, setChatLoading] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState("");

  const updateSessionIds = useCallback((updater) => {
    setSessionIds((prev) => {
      const next = typeof updater === "function" ? updater(prev) : updater;
      const unique = [...new Set(next)].filter((value) => Number.isFinite(value));
      writeSessionIds(unique);
      return unique;
    });
  }, []);

  const setActiveSession = useCallback((sessionId) => {
    setActiveSessionId(sessionId);
    writeActiveSessionId(sessionId);
  }, []);

  const upsertSession = useCallback((session) => {
    if (!session || !Number.isFinite(session.sessionId)) {
      return;
    }
    setSessionsById((prev) => ({
      ...prev,
      [session.sessionId]: session,
    }));
  }, []);

  const loadSessionById = useCallback(
    async (sessionId, makeActive) => {
      setChatLoading(true);
      try {
        const session = await getSession(sessionId);
        upsertSession(session);
        updateSessionIds((prev) => {
          if (prev.includes(session.sessionId)) {
            return prev;
          }
          return [session.sessionId, ...prev];
        });
        if (makeActive) {
          setActiveSession(session.sessionId);
        }
      } finally {
        setChatLoading(false);
      }
    },
    [setActiveSession, updateSessionIds, upsertSession]
  );

  useEffect(() => {
    let isMounted = true;

    async function bootstrap() {
      const storedSessionIds = readSessionIds();
      const storedActiveId = readActiveSessionId();

      if (storedSessionIds.length === 0) {
        if (isMounted) {
          setSessionsLoading(false);
          setActiveSession(null);
        }
        return;
      }

      try {
        const results = await Promise.allSettled(storedSessionIds.map((id) => getSession(id)));
        if (!isMounted) {
          return;
        }

        const loadedSessions = {};
        const validIds = [];
        results.forEach((result) => {
          if (result.status === "fulfilled" && Number.isFinite(result.value.sessionId)) {
            const session = result.value;
            loadedSessions[session.sessionId] = session;
            validIds.push(session.sessionId);
          }
        });

        setSessionsById(loadedSessions);
        updateSessionIds(validIds);

        const restoredActiveId =
          storedActiveId != null && validIds.includes(storedActiveId) ? storedActiveId : null;
        setActiveSession(restoredActiveId);
      } catch (e) {
        if (isMounted) {
          setError(toErrorMessage(e));
        }
      } finally {
        if (isMounted) {
          setSessionsLoading(false);
        }
      }
    }

    bootstrap();

    return () => {
      isMounted = false;
    };
  }, [setActiveSession, updateSessionIds]);

  const activeSession = activeSessionId != null ? sessionsById[activeSessionId] : null;

  const sidebarSessions = useMemo(
    () =>
      Object.values(sessionsById)
        .map((session) => sessionToSidebarEntry(session))
        .sort(sortByUpdatedAtDesc),
    [sessionsById]
  );

  const chatItems = useMemo(() => {
    if (!activeSession) {
      return [];
    }
    return sessionToChatItems(activeSession);
  }, [activeSession]);

  const activeTitle = useMemo(() => {
    const activeEntry = sidebarSessions.find((entry) => entry.id === activeSessionId);
    return activeEntry?.title || null;
  }, [activeSessionId, sidebarSessions]);

  const handleNewChat = useCallback(() => {
    setError("");
    setActiveSession(null);
  }, [setActiveSession]);

  const handleSelectSession = useCallback(
    async (sessionId) => {
      setError("");
      setActiveSession(sessionId);
      try {
        await loadSessionById(sessionId, true);
      } catch (e) {
        setError(toErrorMessage(e));
      }
    },
    [loadSessionById, setActiveSession]
  );

  const handleDeleteSession = useCallback(
    async (sessionId) => {
      setError("");
      try {
        await deleteSession(sessionId);
        setSessionsById((prev) => {
          const next = { ...prev };
          delete next[sessionId];
          return next;
        });
        updateSessionIds((prev) => prev.filter((id) => id !== sessionId));
        if (activeSessionId === sessionId) {
          setActiveSession(null);
        }
      } catch (e) {
        setError(toErrorMessage(e));
      }
    },
    [activeSessionId, setActiveSession, updateSessionIds]
  );

  const handleAnalyze = useCallback(
    async ({ files, text }) => {
      setError("");
      setAnalyzing(true);
      try {
        const formData = new FormData();
        files.forEach((file) => formData.append("files", file));
        if (text) {
          formData.append("text", text);
        }
        if (activeSessionId != null) {
          formData.append("sessionId", String(activeSessionId));
        }

        const { sessionId } = await analyzeLogs(formData);
        const resolvedSessionId = sessionId ?? activeSessionId;
        if (resolvedSessionId == null) {
          throw new Error("Session id was not returned from analysis API.");
        }

        setActiveSession(resolvedSessionId);
        updateSessionIds((prev) => [resolvedSessionId, ...prev.filter((id) => id !== resolvedSessionId)]);
        await loadSessionById(resolvedSessionId, false);
        return true;
      } catch (e) {
        setError(toErrorMessage(e));
        return false;
      } finally {
        setAnalyzing(false);
      }
    },
    [activeSessionId, loadSessionById, setActiveSession, updateSessionIds]
  );

  return (
    <div className="app-shell">
      <Sidebar
        sessions={sidebarSessions}
        activeSessionId={activeSessionId}
        onNewChat={handleNewChat}
        onSelectSession={handleSelectSession}
        onDeleteSession={handleDeleteSession}
        loading={sessionsLoading}
      />

      <main className="chat-panel">
        <ChatHeader activeSessionId={activeSessionId} title={activeTitle} />

        {error && <div className="error-banner">{error}</div>}

        <ChatWindow items={chatItems} loading={analyzing} sessionLoading={chatLoading} />

        <UploadComposer onSubmit={handleAnalyze} loading={analyzing || chatLoading} />
      </main>
    </div>
  );
}
