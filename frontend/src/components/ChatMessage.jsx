import AnalysisCard from "./AnalysisCard";

export default function ChatMessage({ item }) {
  if (item.role === "USER") {
    return (
      <div className="message-row user-row">
        <div className="message-bubble user-bubble">{item.text}</div>
      </div>
    );
  }

  return (
    <div className="message-row assistant-row">
      <AnalysisCard analysis={item.analysis} />
    </div>
  );
}
