import { useContext, useEffect, useState } from "react";
import { SummaryServiceContext } from "../contexts/SummaryServiceContext.tsx";
import type { SummaryResponse } from "../types/summaryResponse.ts";

export function useSummaryService() {
  const ctx = useContext(SummaryServiceContext);
  if (!ctx) {
    throw new Error(
      "useSummaryService must be used within SummaryServiceProvider",
    );
  }
  return ctx;
}

export const useSummaries = () => {
  const service = useSummaryService();
  const [summaries, setSummaries] = useState<SummaryResponse[]>([]);

  useEffect(() => {
    // Load local first (instant)
    setSummaries(service.getLocalSummaries());
    //console.log("Hook subscribed");
    const unsubscribe = service.subscribe(() => {
      //console.log("Hook notified");
      setSummaries(service.getLocalSummaries());
    });
    return () => {
      //console.log("Hook unsubscribed");
      unsubscribe();
    };
  }, [service]);

  return summaries;
};
