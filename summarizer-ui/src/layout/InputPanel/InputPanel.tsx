import styles from "./InputPanel.module.css";
import type { SummaryResponse } from "../../types/summaryResponse.ts";
import { useCallback, useState } from "react";
import type { SummaryLength } from "../../enum/summary-length.ts";
import { LENGTH_LABEL } from "../../constants/length-label.ts";

import type { AxiosResponse } from "axios";
import { useSummaryService } from "../../utils/hooks.ts";
import makeLog from "../../utils/logger.ts";

interface InputPanelProps {
  onSubmit: (summary: SummaryResponse) => void;
}
const { log } = makeLog("InputPanel");

export default function InputPanel({ onSubmit }: InputPanelProps) {
  type InputMode = "text" | "url" | "file";

  const [mode, setMode] = useState<InputMode>("text");
  const [text, setText] = useState("");
  const [url, setUrl] = useState("");
  const [file, setFile] = useState<Blob>();
  const [length, setLength] = useState<SummaryLength>("SHORT");
  const [loading, setLoading] = useState(false);
  const summaryService = useSummaryService();

  const handleSubmit = useCallback(async () => {
    const content = mode === "text" ? text : mode === "url" ? url : "file";

    setLoading(true);

    let res: AxiosResponse<SummaryResponse>;
    try {
      log("Starting summarization...");
      res = await summaryService.summarize({ mode, content, file, length });
      log("Finished Summarization");
      onSubmit(res.data);
    } catch (e) {
      console.log(`Error when submitting: ${e}`);
    }
    setText("");
    setUrl("");
    setFile(undefined);
    setLoading(false);
  }, [
    mode,
    text,
    url,
    file,
    length,
    onSubmit,
    setFile,
    setMode,
    setText,
    setLoading,
  ]);

  return (
    <div className={styles.container}>
      {/* Header */}
      <div>
        <h2 className={styles.h2}>New Summary</h2>
        <p className={styles.p}>Paste text, enter a URL, or upload a file</p>
      </div>

      {/* Mode tabs */}
      <div className={styles.tabContainer}>
        {(["text", "url", "file"] as InputMode[]).map((m) => (
          <button
            key={m}
            onClick={() => setMode(m)}
            className={`${styles.tab} ${
              mode === m ? styles.tabActive : styles.tabInactive
            }`}
          >
            {m === "text" ? "Text" : m === "url" ? "URL" : "File"}
          </button>
        ))}
      </div>

      {/* Input Area */}
      {mode === "text" && (
        <textarea
          placeholder="Type or paste text here (minimum 10 words)"
          value={text}
          onChange={(e) => setText(e.target.value)}
          className={`${styles.base} ${styles.textarea}`}
        />
      )}

      {mode === "url" && (
        <input
          type="url"
          placeholder="https://example.com/article"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          className={`${styles.base} ${styles.url}`}
        />
      )}

      {mode === "file" && (
        <label className={styles.file}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path
              d="M12 16V8M12 8L9 11M12 8l3 3"
              stroke="#8B8FA8"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M4 16v1a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3v-1"
              stroke="#8B8FA8"
              strokeWidth="1.5"
              strokeLinecap="round"
            />
          </svg>
          <span>Click to upload a PDF or text file</span>
          <input
            type="file"
            accept=".pdf,.txt,.doc,.docx"
            onChange={(e) => setFile(e.target.files?.[0])}
          />
        </label>
      )}

      {/* Length Selector */}
      <div>
        <label className={styles.lengthLabel}>Summary length</label>

        <div className={styles.lengthGrid}>
          {(["TLDR", "SHORT", "MEDIUM", "DETAILED"] as SummaryLength[]).map(
            (l) => (
              <button
                key={l}
                onClick={() => setLength(l)}
                className={`${styles.lengthButton} ${
                  length === l ? "" : styles.lengthButtonInactive
                }`}
              >
                {LENGTH_LABEL[l]}
              </button>
            ),
          )}
        </div>
      </div>

      {/* Word count hint */}
      {mode === "text" && text.trim() && (
        <div className={styles.hintContainer}>
          <span className={styles.hintTitle}>Word count</span>
          <span className={styles.hintText}>
            {text.trim().split(/\s+/).length.toLocaleString()}
          </span>
        </div>
      )}

      <div style={{ flex: 1 }} />

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={
          loading ||
          (mode === "text" && text.trim().split(/\s+/).length < 10) ||
          (mode === "url" && !url.trim()) ||
          (mode === "file" && !file)
        }
        className={`${styles.submit} ${
          loading ? styles.loading : styles.notLoading
        }`}
      >
        {loading ? "Summarizing..." : "Summarize"}
      </button>
    </div>
  );
}
