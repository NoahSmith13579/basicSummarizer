import type {SourceType} from "../enum/source-type.ts";
import type {LocalDateTime} from "./localDateTime.ts";
import type {SummaryLength} from "../enum/summary-length.ts";

export type SummaryResponse = {
    id: number;
    summary: string;
    originalWordCount: number;
    summaryWordCount: number;
    compressionRatio: number;
    sourceType: SourceType;
    sourceUrl?: string;
    summaryLength: SummaryLength;
    savedAt: LocalDateTime;
}
