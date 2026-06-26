import type { SummaryListItem } from "../../types/summaryListItem.ts";
import type { ReactNode } from "react";
import styles from "./Sidebar.module.css";
import Brand from "./components/Brand.tsx";
import FilterNav from "./components/FilterNav.tsx";
import RecentList from "./components/RecentList.tsx";
import { StatsFooter } from "./components/StatsFooter.tsx";

interface SidebarProps {
    summaries: SummaryListItem[];
    activeFilter: string;
    onFilterChange: (filter: string) => void;
    selectedId: number | null;
    onSelect: (id: number) => void;
}

export default function Sidebar({
                                    summaries,
                                    activeFilter,
                                    onFilterChange,
                                    selectedId,
                                    onSelect,
                                }: SidebarProps): ReactNode {
    const counts = {
        all: summaries.length,
        TEXT: summaries.filter((summary) => summary.sourceType == "TEXT").length,
        URL: summaries.filter((summary) => summary.sourceType == "URL").length,
        FILE: summaries.filter((summary) => summary.sourceType == "FILE").length,
    };

    return (
        <div className={styles.container}>
            <Brand />
            <FilterNav
                activeFilter={activeFilter}
                onFilterChange={onFilterChange}
                counts={counts}
            />
            <RecentList
                summaries={summaries}
                selectedId={selectedId}
                onSelect={onSelect}
            />
            <StatsFooter summaries={summaries} />
        </div>
    );
}
