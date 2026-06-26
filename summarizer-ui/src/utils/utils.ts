import type {SummaryResponse} from "../types/summaryResponse.ts";
import type {SummaryListItem} from "../types/summaryListItem.ts";

export function truncate(text: string, max: number) {
    return text.length <= max ? text : text.slice(0, max).trimEnd() + "…";
}

export function mapSummaryResponseToListItem(
    s: SummaryResponse
): SummaryListItem {
    return {
        id: s.id,
        title: truncate(s.summary, 30),
        createdAt: s.savedAt.toString(),
        summaryLength: s.summaryLength,
        sourceType: s.sourceType,
        compressionRatio: s.compressionRatio
    };
}