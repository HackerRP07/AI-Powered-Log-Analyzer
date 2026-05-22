function severityClass(severity) {
  const normalized = (severity || "").toUpperCase();
  if (normalized === "CRITICAL") {
    return "severity-critical";
  }
  if (normalized === "HIGH") {
    return "severity-high";
  }
  if (normalized === "MEDIUM") {
    return "severity-medium";
  }
  if (normalized === "LOW") {
    return "severity-low";
  }
  return "severity-unknown";
}

export default function FindingsList({ findings }) {
  if (!findings || findings.length === 0) {
    return <p className="analysis-empty">No major findings detected.</p>;
  }

  return (
    <div className="findings-list">
      {findings.map((finding, index) => {
        const severity = (finding?.severity || "UNKNOWN").toUpperCase();
        const message = finding?.message || "No finding message provided.";
        const evidence = finding?.evidence || "No supporting evidence provided.";

        return (
          <article key={`${severity}-${index}`} className="finding-item">
            <div className="finding-header">
              <span className={`severity-badge ${severityClass(severity)}`}>{severity}</span>
            </div>
            <p className="finding-message">{message}</p>
            <p className="finding-evidence">
              <strong>Evidence:</strong> {evidence}
            </p>
          </article>
        );
      })}
    </div>
  );
}
