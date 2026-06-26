
// SavedSummary from the backend

import type {User} from "./user.ts";
import type {LocalDateTime} from "./localDateTime.ts";

export interface Summary {
    id: number,
    user: User,
    originalText: string,
    summary: string,
    sourceType: string,
    sourceUrl: string | null,
    originalWordCount: number,
    summaryWordCount: number,
    summaryLength: string,
    createdAt: LocalDateTime
}