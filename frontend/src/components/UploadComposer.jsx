import { useState } from "react";

export default function UploadComposer({ onSubmit, loading }) {
  const [contextText, setContextText] = useState("");
  const [files, setFiles] = useState([]);

  async function handleSubmit(event) {
    event.preventDefault();
    if (files.length === 0) {
      return;
    }

    const success = await onSubmit({
      files,
      text: contextText.trim(),
    });

    if (success) {
      setContextText("");
      setFiles([]);
      if (event.target?.reset) {
        event.target.reset();
      }
    }
  }

  return (
    <form className="composer" onSubmit={handleSubmit}>
      <div className="composer-top">
        <input
          type="file"
          multiple
          name="files"
          className="file-input"
          onChange={(event) => setFiles(Array.from(event.target.files || []))}
          disabled={loading}
        />
        <textarea
          placeholder="Optional: tell the AI what issue to focus on..."
          value={contextText}
          onChange={(event) => setContextText(event.target.value)}
          disabled={loading}
        />
      </div>

      <div className="composer-bottom">
        <div className="file-count">
          {files.length > 0 ? `${files.length} file(s) selected` : "No files selected"}
        </div>
        <button type="submit" disabled={loading || files.length === 0}>
          {loading ? "Analyzing..." : "Send"}
        </button>
      </div>
    </form>
  );
}
