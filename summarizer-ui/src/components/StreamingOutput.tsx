import {useEffect, useRef, useState} from "react";
import type {SummaryLength} from "../enum/summary-length.ts";

interface StreamingOutputProps {
    text: string;
    summaryLength: SummaryLength;
}

export default function StreamingOutput ({text, summaryLength}: StreamingOutputProps){
    const [output, setOutput] = useState("");
    const esRef = useRef<null | EventSource>(null);

    useEffect(()=>{
        if (!text) return;
        setOutput('');

        //const token = sessionStorage.getItem("token")

        const baseURL = import.meta.env.VITE_NODE_ENV == "development" ?
            import.meta.env.VITE_DEV_API_URL:
            import.meta.env.VITE_PROD_API_URL

        const url = `${baseURL}api/v1/summarize/stream` +
            `?text=${encodeURIComponent(text)}`+
            `&summaryLength=${summaryLength}`;

        const es= new EventSource(url);
        esRef.current = es;

        es.onmessage = (e) => {
            setOutput((prev) => prev +e.data);
        }

        es.onerror= () => {
            es.close();
        }

        return ()=> {
            es.close();
        }
    }, [text, summaryLength])
    return <pre style={{whiteSpace: "pre-wrap"}}>{output}</pre>
}