import clientAPI from "../api/client.ts";
import type { SummaryLength } from "../enum/summary-length.ts";

export class ApiSummaryRepository {
  async summarizeText(text: string, summaryLength: SummaryLength) {
    return clientAPI.post("summarize", { text, summaryLength });
  }

  async summarizeURL(url: string, summaryLength: SummaryLength) {
    return clientAPI.post("summarize/url", { url, summaryLength });
  }

  async summarizeFile(file: Blob, summaryLength: SummaryLength) {
    const form = new FormData();
    form.append("file", file);
    form.append("summaryLength", summaryLength);
    return clientAPI.post("summarize/file", form);
  }

  async getSummaries() {
    return clientAPI.get("api/v1/summarize/");
  }

  async getSummary(id: number) {
    return clientAPI.get(`/summarize/${id}`);
  }

  async deleteSummary(id: number) {
    return clientAPI.delete(`/summarize/${id}`);
  }
}
