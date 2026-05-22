import SessionList from "./SessionList";

export default function Sidebar({
  sessions,
  activeSessionId,
  onNewChat,
  onSelectSession,
  onDeleteSession,
  loading,
}) {
  return (
    <aside className="sidebar">
      <button type="button" className="new-chat-btn" onClick={onNewChat}>
        + New Chat
      </button>

      {loading ? (
        <p className="empty-sidebar">Loading sessions...</p>
      ) : (
        <SessionList
          sessions={sessions}
          activeSessionId={activeSessionId}
          onSelect={onSelectSession}
          onDelete={onDeleteSession}
        />
      )}
    </aside>
  );
}
