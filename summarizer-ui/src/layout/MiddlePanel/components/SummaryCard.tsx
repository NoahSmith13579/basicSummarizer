import type { SummaryResponse } from "../../../types/summaryResponse.ts";
import { useState } from "react";
import { SOURCE_META } from "../../../constants/sourceMeta.tsx";
import truncate from "../../../utils/truncate.ts";
import { LENGTH_LABEL } from "../../../constants/length-label.ts";
import { DeleteButton } from "./DeleteButton.tsx";
import styles from "./SummaryCard.module.css";

interface SummaryCardProps {
    summary: SummaryResponse;
    selected?: boolean;
    onDelete?: (id: number) => void;
    onView?: (id: number) => void;
}

export function SummaryCard({
                                summary,
                                selected,
                                onDelete,
                                onView,
                            }: SummaryCardProps) {
    const [hovered, setHovered] = useState(false);
    const source = SOURCE_META[summary.sourceType];
    return (
        <article
            className={`${styles.summaryCardContainer} ${
                selected
                    ? styles.summaryCardContainerSelected
                    : hovered
                        ? styles.summaryCardContainerHovered
                        : ""
            }`}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
            onClick={() => onView?.(summary.id)}
            role={onView ? "button" : undefined}
            tabIndex={onView ? 0 : undefined}
            onKeyDown={(e) => {
                if (onView && (e.key === "Enter" || e.key === " ")) {
                    e.preventDefault();
                    onView(summary.id);
                }
            }}
        >
            {/* Top Row */}
            <div className={styles.topRowContainer}>
                <div className={styles.topRowLabelContainer}>
                    <span
                        className={styles.sourceLabel}
                        style={{
                            background: `${source.color}1A`,
                            border: `1px solid ${source.color}40`,
                            color: source.color,
                        }}
                    >
                        {source.icon} {source.label}
                    </span>

                    <span className={styles.lengthLabel}>
                        {LENGTH_LABEL[summary.summaryLength]}
                    </span>
                </div>

                <div className={styles.arcContainer}>
                    <span className={styles.arcSpan}>
                        {`${summary.compressionRatio.toFixed(2)}% compressed`}
                    </span>
                </div>
            </div>

            {/* Text */}
            <p className={styles.text}>{summary.summary}</p>

            {/* URL */}
            {summary.sourceType === "URL" && summary.sourceUrl && (
                <a
                    href={summary.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={(e) => e.stopPropagation()}
                    className={styles.url}
                >
                    ↗{" "}
                    {truncate(
                        summary.sourceUrl.replace(/^https?:\/\/(www\.)?/, ""),
                        50
                    )}
                </a>
            )}

            <div style={{ height: 1, background: "252836" }} />

            {/* Bottom Row */}
            <div className={styles.bottomRowContainer}>
                <div className={styles.statsContainer}>
                    <span style={{ color: "#35394D", fontSize: 12 }}></span>
                    <span style={{ color: "#35394D", fontSize: 10 }}></span>
                    <span style={{ color: "#8B8FA8", fontSize: 11 }}>
                        #{summary.id}
                    </span>
                </div>

                <div className={styles.buttonContainer}>
                    <span className={styles.date}>{summary.savedAt}</span>

                    {onView && (
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onView(summary.id);
                            }}
                            className={styles.viewButton}
                        >
                            View →
                        </button>
                    )}

                    {onDelete && (
                        <DeleteButton onClick={() => onDelete(summary.id)} />
                    )}
                </div>
            </div>
        </article>
    );
}
