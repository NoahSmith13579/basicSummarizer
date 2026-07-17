import { useCallback, useState } from "react";

import "./App.css";

import { MiddlePanel } from "./layout/MiddlePanel/MiddlePanel";
import SiteLayout from "./layout/SiteLayout.tsx";
import Sidebar from "./layout/Sidebar/Sidebar.tsx";
import type { SummaryResponse } from "./types/summaryResponse.ts";
import InputPanel from "./layout/InputPanel/InputPanel.tsx";
import { DetailPanel } from "./layout/DetailPanel/DetailPanel.tsx";

import { useSummaries, useSummaryService } from "./utils/hooks.ts";

function App() {
  const service = useSummaryService();
  const summaries = useSummaries();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [filter, setFilter] = useState("all");
  const handleDelete = useCallback(
    (id: number) => {
      service.deleteLocalSummary(id);
      console.log("Deleting:", id, "Selected:", selectedId);

      if (selectedId === id) setSelectedId(null);

      console.log("Summaries: " + localStorage.getItem("summaries"));
    },
    [selectedId, setSelectedId],
  );

  const handleNew = useCallback(
    (s: SummaryResponse) => {
      setSelectedId(s.id);
    },
    [setSelectedId],
  );
  const selectedSummary = summaries.find((s) => s.id == selectedId) ?? null;
  return (
    <>
      <SiteLayout
        left={
          <Sidebar
            activeFilter={filter}
            onFilterChange={setFilter}
            selectedId={selectedId}
            onSelect={setSelectedId}
          />
        }
        center={
          <MiddlePanel
            selectedId={selectedId}
            onSelect={setSelectedId}
            onDelete={handleDelete}
            filter={filter}
          />
        }
        right={
          selectedSummary ? (
            <DetailPanel
              summary={selectedSummary}
              onClose={() => setSelectedId(null)}
              onDelete={handleDelete}
            />
          ) : (
            <InputPanel onSubmit={handleNew} />
          )
        }
      />
    </>
  );
}

export default App;
