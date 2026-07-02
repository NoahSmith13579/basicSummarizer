import { createContext, useMemo } from "react";
import { ApiSummaryRepository } from "../adapters/ApiSummaryRepository.ts";
import { LocalSummaryRepository } from "../adapters/LocalSummaryRepository.ts";
import { SummaryService } from "../services/SummaryService.ts";
import * as React from "react";

export const SummaryServiceContext = createContext<SummaryService | null>(null);

export const SummaryServiceProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const summaryService = useMemo(() => {
    const api = new ApiSummaryRepository();
    const local = new LocalSummaryRepository();
    return new SummaryService(api, local);
  }, []);
  return (
    <SummaryServiceContext.Provider value={summaryService}>
      {children}
    </SummaryServiceContext.Provider>
  );
};
