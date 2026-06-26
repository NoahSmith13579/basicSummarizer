import type {SummaryListItem} from "../../../types/summaryListItem.ts";
import styles from "../Sidebar.module.css"

interface StatsFooterProps {
    summaries: SummaryListItem[];
}

export function StatsFooter({ summaries }: StatsFooterProps) {
    let averageRatio: string|null = ((summaries?.reduce((a, s) =>
        a + s.compressionRatio, 0) / summaries.length)
        .toFixed(0) + "%")
        ?? null;

    return (
        <div className={styles.footerContainer}>
            <div className={styles.footerGrid}>
                {[
                    { label: "Total", value: summaries.length },
                    {
                        label: "Avg ratio",
                        value: summaries.length
                            ? averageRatio
                            : "—",
                    },
                ].map((s) => (
                    <div key={s.label} className={styles.footerItem}>
                        <div className={styles.footerItemText}>{s.value}</div>
                        <div className={styles.footerItemData}>{s.label}</div>
                    </div>
                ))}
            </div>
        </div>
    );
}
