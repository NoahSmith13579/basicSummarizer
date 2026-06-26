import type {SourceType} from "../enum/source-type.ts";

export const SOURCE_META: Record<SourceType, { label: string; color: string; icon: React.ReactNode }> = {
    TEXT: {
        label: "Text",
        color: "#6C63FF",
        icon: (
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <rect x="1" y="2" width="10" height="1.5" rx=".75" fill="currentColor" />
                <rect x="1" y="5.25" width="7" height="1.5" rx=".75" fill="currentColor" />
                <rect x="1" y="8.5" width="8.5" height="1.5" rx=".75" fill="currentColor" />
            </svg>
        ),
    },
    FILE: {
        label: "File",
        color: "#F59E42",
        icon: (
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path d="M2 1.5A.5.5 0 0 1 2.5 1H7l3 3v6.5a.5.5 0 0 1-.5.5h-7a.5.5 0 0 1-.5-.5v-9z" stroke="currentColor" strokeWidth="1.2" fill="none" />
                <path d="M7 1v3h3" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" />
            </svg>
        ),
    },
    URL: {
        label: "URL",
        color: "#3ECFCF",
        icon: (
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path d="M5 7a3 3 0 0 0 4.243.243l1.5-1.5a3 3 0 0 0-4.243-4.243L5.414 2.586" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" />
                <path d="M7 5a3 3 0 0 0-4.243-.243l-1.5 1.5a3 3 0 0 0 4.243 4.243L6.586 9.414" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" />
            </svg>
        ),
    },
};
