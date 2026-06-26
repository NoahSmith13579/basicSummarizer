import type { SummaryListItem } from "../../../types/summaryListItem.ts";
import truncate from "../../../utils/truncate.ts";
import { SOURCE_META } from "../../../constants/sourceMeta.tsx";
import styles from "../Sidebar.module.css";

interface RecentListProps {
    summaries: SummaryListItem[];
    selectedId: number | null;
    onSelect: (id: number) => void;
}

export default function RecentList({
                                       summaries,
                                       selectedId,
                                       onSelect,
                                   }: RecentListProps) {
    return (
        <div className={styles.recentContainer}>
            <span className={styles.recentTitle}>Recent</span>

            <div className={styles.recentButtonContainer}>
                {[...summaries].slice(0, 8).map((s) => {
                    const selected = s.id === selectedId;
                    const src = SOURCE_META[s.sourceType];

                    return (
                        <button
                            key={s.id}
                            onClick={() => onSelect(s.id)}
                            className={`${styles.recentButton} ${
                                selected ? styles.recentButtonSelectedColor : ""
                            }`}
                        >
                            <span
                                className={styles.recentButtonIcon}
                                style={{ color: src.color }}
                            >
                                {src.icon}
                            </span>

                            <span
                                className={`${styles.recentButtonText} ${
                                    selected ? styles.recentButtonTextSelected : ""
                                }`}
                            >
                                {truncate(s.title, 30)}
                            </span>
                        </button>
                    );
                })}
            </div>
        </div>
    );
}
