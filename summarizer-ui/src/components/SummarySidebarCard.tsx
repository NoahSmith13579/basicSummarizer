import type {SummaryLength} from "../enum/summary-length.ts";
import type {LocalDateTime} from "../types/localDateTime.ts";

interface SummarySidebarCardProps{
    title: string,
    summaryLength: SummaryLength,
    timestamp: LocalDateTime
}
// TODO: mess with sizes of elements to improve look
export default function SummarySidebarCard({title, summaryLength, timestamp}:SummarySidebarCardProps){
    // TODO: add fade away effect after certain number of characters
    return <div>
        <ul>
            <li>
                {title}
            </li>
            <li>
                {summaryLength}
            </li>
            <li>
                {timestamp}
            </li>
        </ul>

    </div>
}