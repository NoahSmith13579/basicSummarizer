import clientAPI from "./client.ts";
import type {SummaryLength} from "../enum/summary-length.ts";
import type {AxiosResponse} from "axios";
import type {SummaryResponse} from "../types/summaryResponse.ts";


export type SummaryAPI = {
    text: (text: string, length: SummaryLength)=> Promise<any>;
    url: (url: string, length: SummaryLength)=> Promise<any>;
    file: (file:Blob)=> Promise<any>;
}


//TODO: Add error handler
export const summarizeText = async (text: string, summaryLength: SummaryLength): Promise<AxiosResponse<SummaryResponse>> => {
    return await clientAPI.post("summarize", {text, summaryLength})
}
export const summarizeURL = async (url: string): Promise<AxiosResponse<SummaryResponse>> =>{
    return await clientAPI.post("summarize/url", {url});
}
export const summarizeFile= async (file: Blob, summaryLength: SummaryLength): Promise<AxiosResponse<SummaryResponse>> =>{
    const form = new FormData();
    form.append("file", file);
    form.append("summaryLength", summaryLength);
    return await clientAPI.post("summarize/file", form)
}

export const getSummaries = async () => await clientAPI.get('/summarize');
export const getSummary   = async (id: number) => await clientAPI.get(`/summarize/${id}`);
export const deleteSummary = async (id: number) => await clientAPI.delete(`/summarize/${id}`);
export const getStats     = async () => await clientAPI.get('/summarize/stats/overview');