import { useEffect, useRef } from "react";
import ChatMessage from "./ChatMessage";

export default function ChatWindow({ items, loading, sessionLoading }) {
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [items, loading]);

  return (
    <div className="chat-window">
      {items.length === 0 && !loading && !sessionLoading && (
        <div className="empty-chat">
          Upload logs/images and optionally add issue context to start analysis.
        </div>
      )}

      {sessionLoading && <div className="loading-session">Loading selected chat...</div>}
      {items.map((item) => (
        <ChatMessage key={item.id} item={item} />
      ))}

      {loading && <div className="loading-analysis">Analyzing files...</div>}
      <div ref={endRef} />
    </div>
  );
}
