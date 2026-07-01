import { type ReactNode } from "react";
import styles from "./Sidebar.module.css";
import Brand from "./components/Brand.tsx";
import FilterNav from "./components/FilterNav.tsx";
import RecentList from "./components/RecentList.tsx";
import { StatsFooter } from "./components/StatsFooter.tsx";
import { useSummaries } from "../../utils/hooks.ts";
import { mapSummaryResponseToListItem } from "../../utils/utils.ts";

interface SidebarProps {
  activeFilter: string;
  onFilterChange: (filter: string) => void;
  selectedId: number | null;
  onSelect: (id: number) => void;
}

export default function Sidebar({
  activeFilter,
  onFilterChange,
  selectedId,
  onSelect,
}: SidebarProps): ReactNode {
  const summaryResponses = useSummaries();

  let summaries = summaryResponses.map((summary) =>
    mapSummaryResponseToListItem(summary),
  );

  const counts = {
    all: summaries.length,
    TEXT: summaries.filter((summary) => summary.sourceType == "TEXT").length,
    URL: summaries.filter((summary) => summary.sourceType == "URL").length,
    FILE: summaries.filter((summary) => summary.sourceType == "FILE").length,
  };
  console.log("Sidebar summaries:", summaryResponses);

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
