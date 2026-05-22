function formatUpdatedAt(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return date.toLocaleString();
}

export default function SessionList({ sessions, activeSessionId, onSelect, onDelete }) {
  if (sessions.length === 0) {
    return <p className="empty-sidebar">No saved chats yet.</p>;
  }

  return (
    <ul className="session-list">
      {sessions.map((session) => (
        <li
          key={session.id}
          className={`session-item ${activeSessionId === session.id ? "active" : ""}`}
          onClick={() => onSelect(session.id)}
        >
          <div className="session-content">
            <div className="session-title">{session.title}</div>
            <div className="session-preview">{session.preview}</div>
            <div className="session-time">{formatUpdatedAt(session.updatedAt)}</div>
          </div>
          <button
            type="button"
            className="delete-session-btn"
            onClick={(event) => {
              event.stopPropagation();
              onDelete(session.id);
            }}
            title="Delete chat"
          >
            Delete
          </button>
        </li>
      ))}
    </ul>
  );
}
