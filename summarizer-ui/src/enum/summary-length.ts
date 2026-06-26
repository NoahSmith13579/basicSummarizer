export type SummaryLength = "TLDR" | "SHORT" | "MEDIUM" | "DETAILED";

export const SummaryLengthMeta = {
    TLDR: {
        description: "1-2 sentences",
        targetRatio: 0.1,
    },
    SHORT: {
        description: "3-5 sentences",
        targetRatio: 0.2,
    },
    MEDIUM: {
        description: "5-10 sentences",
        targetRatio: 0.4,
    },
    DETAILED: {
        description: "Comprehensive",
        targetRatio: 0.6,
    },
} as const;