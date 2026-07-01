import type { SummaryResponse } from "../types/summaryResponse.ts";
import type { SummaryRepository } from "../repositories/SummaryRepository.ts";

export class LocalSummaryRepository implements SummaryRepository {
  private key = "summaries";

  private read(): SummaryResponse[] {
    const raw = localStorage.getItem(this.key);
    return raw ? JSON.parse(raw) : [];
  }

  private write(data: SummaryResponse[]) {
    localStorage.setItem(this.key, JSON.stringify(data));
  }

  save(summary: SummaryResponse) {
    const all = this.read();
    all.push(summary);
    this.write(all);
  }

  getAll() {
    return this.read();
  }

  get(id: number) {
    return this.read().find((s) => s.id === id);
  }

  delete(id: number) {
    const filtered = this.read().filter((s) => s.id !== id);
    this.write(filtered);
  }
}
