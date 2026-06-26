import { NAV_ITEMS } from "../../../enum/filter-nav.ts";
import type { SummaryCountProps } from "../summary-count-props.ts";
import styles from "../Sidebar.module.css";

interface FilterNavProps {
    activeFilter: string;
    onFilterChange: (filter: string) => void;
    counts: SummaryCountProps;
}

export default function FilterNav({
                                      activeFilter,
                                      onFilterChange,
                                      counts,
                                  }: FilterNavProps) {
    const colorActiveButton = getComputedStyle(document.documentElement)
        .getPropertyValue("--active-button")
        .trim();

    return (
        <div className={styles.filterContainer}>
            <span className={styles.filterTitle}>Library</span>

            {NAV_ITEMS.map((item) => {
                const active = activeFilter === item.id;
                const count = counts[item.id];

                return (
                    <button
                        key={item.id}
                        onClick={() => onFilterChange(item.id)}
                        className={styles.filterButton}
                        style={{
                            background: active ? colorActiveButton : "transparent",
                        }}
                    >
                        <span
                            className={`${styles.filterButtonLabel} ${
                                active ? styles.activeButton : ""
                            }`}
                        >
                            {item.label}
                        </span>

                        <span
                            className={`${styles.filterButtonCount} ${
                                active
                                    ? styles.filterButtonCountActiveColors
                                    : styles.filterButtonCountInactiveColors
                            }`}
                        >
                            {count}
                        </span>
                    </button>
                );
            })}
        </div>
    );
}
