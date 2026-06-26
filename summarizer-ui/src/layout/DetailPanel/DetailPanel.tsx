
import styles from "./DetailPanel.module.css"
import {SOURCE_META} from "../../constants/sourceMeta.tsx";
import {DeleteButton} from "../MiddlePanel/components/DeleteButton.tsx";
import type {SummaryResponse} from "../../types/summaryResponse.ts";
import {LENGTH_LABEL} from "../../constants/length-label.ts";

interface DetailPanelProps{
    summary: SummaryResponse;
    onClose: () => void;
    onDelete: (id: number) => void;
}

// TODO: may have fucked up the function props calls
export function DetailPanel({summary,onClose,onDelete}: DetailPanelProps){
    const source = SOURCE_META[summary.sourceType];
    const hue = Math.round(180 + Math.min(Math.max(summary.compressionRatio,0),100)/100*90);
    const accent = `hsl(${hue},70%,62%)`;

    return(
        <div className={styles.container}>
            <div className={styles.topRow}>
                <button onClick={onClose} className={styles.newButton}>
                    ← New summary
                </button>
            <DeleteButton onClick={()=>{onDelete(summary.id); onClose();}}/>
            </div>

            <div className={styles.metaBlock}>
                <span
                    className={styles.sourceLabel}
                    style={{background: `${source.color}1A`,
                        border:`1px solid ${source.color}40`,
                        color: source.color}}
                >
                    {source.icon} {source.label}
                </span>
                <span className={styles.sourceLength}>
                    {LENGTH_LABEL[summary.summaryLength]}
                </span>
                <span className={styles.sourceId}>
                    #{summary.id}
                </span>
            </div>
            <div className={styles.grid}>
                {[
                    { label:"Original", value:summary.originalWordCount, unit:"words" },
                    { label:"Summary", value:summary.summaryWordCount, unit:"words" },
                    { label:"Compressed", value:summary.compressionRatio.toFixed(0)+"%", unit:"" },
                ].map((s)=>(
                    <div
                        key={s.label}
                        className={styles.gridContainer}
                    >
                        <div
                            className={styles.gridUnit}
                            style={{
                                color: s.label == "Compressed"? accent: "#F0EEF8"
                            }}
                        >
                            {typeof s.value=="number"? s.value.toLocaleString():
                                s.value
                            } <span style={{fontSize:10, color: "8B8FA8", marginLeft: 2}}>{s.unit}</span>
                        </div>
                        <div className={styles.gridLabel}>
                            {s.label}
                        </div>
                    </div>
                ))}
            </div>
            {summary.sourceType=="URL" && summary.sourceUrl &&(
                <a className={styles.sourceURL}>
                    {summary.sourceUrl.replace(/^https?:\/\/(www\.)?/,"")}
                </a>
            )}
            <div className={styles.horizontal}/>
            <div className={styles.summaryContainer}>
                <p className={styles.summaryTitle}>Summary</p>
                <p className={styles.summaryText}>{summary.summary}</p>
            </div>
            <div className={styles.footer}>
                <span className={styles.footerText}>
                    Saved {summary.savedAt}
                </span>
            </div>
        </div>
    )
}