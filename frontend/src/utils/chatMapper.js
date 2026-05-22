function normalizeUserMessage(content) {
  if (!content || content.trim() === "") {
    return "Uploaded files for analysis.";
  }
  if (content === "(no additional user context provided)") {
    return "Uploaded files for analysis.";
  }
  return content;
}

export function sessionToSidebarEntry(session) {
  const userMessage = (session.messages || []).find((m) => m.role === "USER");
  const latestResult = (session.analysisResults || [])[session.analysisResults.length - 1];

  const title = userMessage?.content && userMessage.content !== "(no additional user context provided)"
    ? userMessage.content.slice(0, 40)
    : latestResult?.summary?.slice(0, 40) || `Session #${session.sessionId}`;

  const preview = latestResult?.summary || userMessage?.content || "No messages yet";

  return {
    id: session.sessionId,
    title,
    preview,
    updatedAt: session.updatedAt || session.createdAt,
  };
}

export function sessionToChatItems(session) {
  const userItems = (session.messages || [])
    .filter((m) => m.role === "USER")
    .map((m) => ({
      id: `u-${m.id}`,
      role: "USER",
      createdAt: m.createdAt,
      text: normalizeUserMessage(m.content),
    }));

  const assistantItems = (session.analysisResults || []).map((result) => ({
    id: `a-${result.id}`,
    role: "ASSISTANT",
    createdAt: result.createdAt,
    analysis: result,
  }));

  return [...userItems, ...assistantItems].sort((a, b) => {
    const t1 = new Date(a.createdAt || 0).getTime();
    const t2 = new Date(b.createdAt || 0).getTime();
    if (t1 === t2) {
      return a.role === "USER" ? -1 : 1;
    }
    return t1 - t2;
  });
}
