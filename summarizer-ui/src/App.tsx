import {useCallback, useEffect, useState} from 'react'

import './App.css'

//import InputPanel from "./layout/InputPanel/InputPanel"
import {MiddlePanel} from "./layout/MiddlePanel/MiddlePanel"
import SiteLayout from "./layout/SiteLayout.tsx";
import Sidebar from "./layout/Sidebar/Sidebar.tsx";
import type {SummaryResponse} from "./types/summaryResponse.ts";
import type {SummaryListItem} from "./types/summaryListItem.ts";
import {mapSummaryResponseToListItem} from "./utils/utils.ts";
import InputPanel from "./layout/InputPanel/InputPanel.tsx";
import {DetailPanel} from "./layout/DetailPanel/DetailPanel.tsx";

function App() {
  const SEED_SUMMARIES: SummaryResponse[] = [
        {
            id: 42, summary: "The study concludes that remote work increases individual productivity by up to 13% while reducing commute-related stress, though collaboration and spontaneous knowledge transfer remain challenging without intentional structure.",
            originalWordCount: 9, summaryWordCount: 5, compressionRatio: 44.44, sourceType: "TEXT", summaryLength: "SHORT", savedAt: "2024-06-01T12:00:00",
        },
        {
            id: 91, summary: "NASA's Artemis program aims to return humans to the Moon by 2026 with a focus on sustainable long-term presence, including the first woman and first person of color on the lunar surface.",
            originalWordCount: 847, summaryWordCount: 156, compressionRatio: 81.58, sourceType: "URL", sourceUrl: "https://nasa.gov/artemis/overview", summaryLength: "MEDIUM", savedAt: "2024-05-28T09:14:00",
        },
        {
            id: 7, summary: "Q4 2024 earnings exceeded expectations with revenue of $4.2B (+18% YoY). EBITDA margins expanded 3pp to 31%. Full-year guidance raised.",
            originalWordCount: 2341, summaryWordCount: 58, compressionRatio: 97.52, sourceType: "FILE", summaryLength: "TLDR", savedAt: "2024-04-12T16:30:00",
        },
    ];
  const [summaries, setSummaries] = useState<SummaryResponse[]>(SEED_SUMMARIES);
  const [listItems, setListItems] = useState<SummaryListItem[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [filter, setFilter] = useState("all");
  const handleDelete = useCallback((id: number) => {
        setSummaries(prev => prev.filter(s => s.id !== id));
        if (selectedId === id) setSelectedId(null);
    }, [selectedId, setSelectedId, setSummaries]);
  useEffect(() => {
        setListItems(summaries.map(mapSummaryResponseToListItem));
    }, [summaries]);
    const handleNew = useCallback((s: SummaryResponse) => {
        setSummaries((prev) => [s, ...prev]);
        setSelectedId(s.id);
    }, [setSelectedId, setSummaries]);
    const selectedSummary= summaries.find((s)=> s.id == selectedId) ?? null
    return (
    <>
      <SiteLayout
          left={<Sidebar summaries={listItems}
                         activeFilter={filter}
                         onFilterChange={setFilter}
                         selectedId={selectedId}
                         onSelect={setSelectedId}/>}
          center={<MiddlePanel summaries={summaries} selectedId={selectedId} onSelect={setSelectedId}
                               onDelete={handleDelete} filter={filter}/>}
          right={
            selectedSummary
              ? <DetailPanel summary={selectedSummary} onClose={()=>setSelectedId(null)} onDelete={handleDelete}/>
              :<InputPanel onSubmit={handleNew}/>
      }
      />
    </>
  )
}

export default App
