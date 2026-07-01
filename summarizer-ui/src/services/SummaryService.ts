import type { SummaryLength } from "../enum/summary-length.ts";
import type { ApiSummaryRepository } from "../adapters/ApiSummaryRepository.ts";
import type { LocalSummaryRepository } from "../adapters/LocalSummaryRepository.ts";
import type { AxiosResponse } from "axios";
import type { SummaryResponse } from "../types/summaryResponse.ts";

type SummarizeInputs = {
  mode: "text" | "url" | "file";
  content?: string;
  file?: Blob;
  length: SummaryLength;
};

export class SummaryService {
  private readonly api: ApiSummaryRepository;
  private readonly local: LocalSummaryRepository;
  private listeners = new Set<() => void>();

  constructor(api: ApiSummaryRepository, local: LocalSummaryRepository) {
    this.local = local;
    this.api = api;
  }

  subscribe(listener: () => void) {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }
  private notify() {
    this.listeners.forEach((l) => l());
  }

  getApi() {
    return this.api;
  }
  getLocal() {
    return this.local;
  }

  async summarize(input: SummarizeInputs) {
    let res: AxiosResponse<SummaryResponse>;

    switch (input.mode) {
      case "text":
        res = await this.summarizeText(input.content!, input.length);
        break;
      case "url":
        res = await this.summarizeURL(input.content!);
        break;
      case "file":
        res = await this.summarizeFile(input.file!, input.length);
    }
    this.local.save(res.data);
    this.notify();
    return res;
  }

  private async summarizeText(text: string, summaryLength: SummaryLength) {
    return await this.api.summarizeText(text, summaryLength);
  }

  private async summarizeURL(url: string) {
    return await this.api.summarizeURL(url);
  }

  private async summarizeFile(file: Blob, summaryLength: SummaryLength) {
    return await this.api.summarizeFile(file, summaryLength);
  }

  getLocalSummaries() {
    return this.local.getAll();
  }

  getLocalSummary(id: number) {
    return this.local.get(id);
  }

  deleteLocalSummary(id: number) {
    this.local.delete(id);
    this.notify();
  }
}
