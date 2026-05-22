export default function RecommendationList({ recommendations }) {
  if (!recommendations || recommendations.length === 0) {
    return <p className="analysis-empty">No recommendations available.</p>;
  }

  return (
    <ul className="recommendation-list">
      {recommendations.map((item, index) => (
        <li key={`${item}-${index}`}>{item}</li>
      ))}
    </ul>
  );
}
