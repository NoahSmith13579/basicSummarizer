import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import {SummaryServiceProvider} from "./contexts/SummaryServiceContext.tsx"

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <SummaryServiceProvider>
            <App/>
        </SummaryServiceProvider>
    </StrictMode>,
)
