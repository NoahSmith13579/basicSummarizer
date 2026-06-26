import type {SummaryLength} from "../enum/summary-length.ts";
import type {SourceType} from "../enum/source-type.ts";

export interface SummaryListItem {
    id: number;
    title: string;
    createdAt: string;
    summaryLength: SummaryLength;
    sourceType: SourceType;
    compressionRatio: number;
}