import type { SummaryResponse } from "../types/summaryResponse.ts";
import type { SummaryRepository } from "../repositories/SummaryRepository.ts";

export class LocalSummaryRepository implements SummaryRepository {
  private summaryKey = "summaries";
  private nextIdKey = "nextId";

  private readSummaries(): SummaryResponse[] {
    const raw = localStorage.getItem(this.summaryKey);
    return raw ? JSON.parse(raw) : [];
  }
  private readNextId(): number {
    const raw = localStorage.getItem(this.nextIdKey);
    return raw ? JSON.parse(raw) : 1;
  }

  private write(data: SummaryResponse[]) {
    localStorage.setItem(this.summaryKey, JSON.stringify(data));
  }

  save(summary: SummaryResponse) {
    const all = this.readSummaries();
    summary = this.assignId(summary);
    all.push(summary);
    this.write(all);
  }

  getAll() {
    return this.readSummaries();
  }

  get(id: number) {
    return this.readSummaries().find((s) => s.id === id);
  }

  delete(id: number) {
    const filtered = this.readSummaries().filter((s) => s.id !== id);
    this.write(filtered);
  }
  updateId() {
    let currentId = this.readNextId();
    localStorage.setItem(this.nextIdKey, JSON.stringify(currentId + 1));
  }
  assignId(summary: SummaryResponse) {
    summary.id = this.readNextId();
    this.updateId();
    return summary;
  }
}
