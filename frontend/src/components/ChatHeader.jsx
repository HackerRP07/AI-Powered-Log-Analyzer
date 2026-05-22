export default function ChatHeader({ activeSessionId, title }) {
  const subtitle = title
    ? title
    : activeSessionId
      ? `Session #${activeSessionId}`
      : "New chat";

  return (
    <header className="chat-header">
      <div>
        <h1>Log Analyser</h1>
        <p className="chat-subtitle">{subtitle}</p>
      </div>
    </header>
  );
}
