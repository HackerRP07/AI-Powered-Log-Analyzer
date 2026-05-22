export default function TimelineList({ timeline }) {
  if (!timeline || timeline.length === 0) {
    return <p className="analysis-empty">No timeline events extracted.</p>;
  }

  const sorted = timeline
    .map((entry, index) => {
      const parsed = Date.parse(entry?.timestamp || "");
      return {
        entry,
        index,
        time: Number.isNaN(parsed) ? Number.POSITIVE_INFINITY : parsed,
      };
    })
    .sort((left, right) => {
      if (left.time === right.time) {
        return left.index - right.index;
      }
      return left.time - right.time;
    });

  return (
    <div className="timeline-list">
      {sorted.map(({ entry }, index) => (
        <article key={`${entry?.timestamp || "no-time"}-${index}`} className="timeline-item">
          <p className="timeline-time">{entry?.timestamp || "Unknown time"}</p>
          <p className="timeline-event">{entry?.event || "No event details available."}</p>
          <p className="timeline-source">
            <strong>Source:</strong> {entry?.source || "Unknown source"}
          </p>
        </article>
      ))}
    </div>
  );
}
