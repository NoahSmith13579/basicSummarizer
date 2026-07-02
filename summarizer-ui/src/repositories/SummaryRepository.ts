import type {SummaryResponse} from "../types/summaryResponse.ts";

export interface SummaryRepository {
    save(summary: SummaryResponse): void;
    getAll(): SummaryResponse[];
    get(id: number): SummaryResponse | undefined;
    delete(id: number): void;
}
