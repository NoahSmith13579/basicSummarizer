import type { SummaryResponse } from "../../types/summaryResponse.ts";
import { SummaryCard } from "./components/SummaryCard.tsx";
import styles from "./MiddlePanel.module.css";

interface MiddlePanelProps {
    summaries: SummaryResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    onDelete: (id: number) => void;
    filter: string;
}

export function MiddlePanel({
                                summaries,
                                selectedId,
                                onSelect,
                                onDelete,
                                filter,
                            }: MiddlePanelProps) {
    const filtered =
        filter === "all"
            ? summaries
            : summaries.filter((summary) => summary.sourceType === filter);

    // Empty state
    if (filtered.length === 0) {
        return (
            <div className={`${styles.container} ${styles.containerEmpty}`}>
                <div className={styles.emptySvg}>
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                        <rect x="3" y="5" width="18" height="3" rx="1.5" fill="#8B8FA8" />
                        <rect
                            x="3"
                            y="10.5"
                            width="13"
                            height="3"
                            rx="1.5"
                            fill="#8B8FA8"
                            opacity=".6"
                        />
                        <rect
                            x="3"
                            y="16"
                            width="15"
                            height="3"
                            rx="1.5"
                            fill="#8B8FA8"
                            opacity=".3"
                        />
                    </svg>
                </div>
                <p className={styles.emptyText}>No Summaries</p>
            </div>
        );
    }

    return (
        <div className={`${styles.container} ${styles.containerFull}`}>
            <div className={styles.containerFlex}>
                <span className={styles.containerTitle}>
                    {filtered.length} {filtered.length === 1 ? "summary" : "summaries"}
                </span>
            </div>

            {filtered.map((summary) => {

                return(
                <SummaryCard
                    key={summary.id}
                    summary={summary}
                    selected={summary.id == selectedId}
                    onView={onSelect}
                    onDelete={onDelete}
                />)
            })}
        </div>
    );
}
