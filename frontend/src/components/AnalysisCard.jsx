import FindingsList from "./FindingsList";
import RecommendationList from "./RecommendationList";
import TimelineList from "./TimelineList";

function formatConfidence(score) {
  const numeric = Number(score);
  if (!Number.isFinite(numeric)) {
    return "0%";
  }
  const percentage = numeric <= 1 ? numeric * 100 : numeric;
  return `${Math.max(0, Math.min(100, Math.round(percentage)))}%`;
}

export default function AnalysisCard({ analysis }) {
  const summary = analysis?.summary || "No summary available.";
  const findings = analysis?.findings || [];
  const timeline = analysis?.timeline || [];
  const recommendations = analysis?.recommendations || [];
  const confidence = formatConfidence(analysis?.confidenceScore);

  return (
    <div className="analysis-card">
      <section className="analysis-section">
        <h3>Summary</h3>
        <p>{summary}</p>
      </section>

      <section className="analysis-section">
        <h3>Findings</h3>
        <FindingsList findings={findings} />
      </section>

      <section className="analysis-section">
        <h3>Timeline</h3>
        <TimelineList timeline={timeline} />
      </section>

      <section className="analysis-section">
        <h3>Recommendations</h3>
        <RecommendationList recommendations={recommendations} />
      </section>

      <div className="confidence">
        Confidence: <span className="confidence-value">{confidence}</span>
      </div>
    </div>
  );
}
