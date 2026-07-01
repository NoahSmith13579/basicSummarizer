import {type ReactNode} from "react";
import styles from "./SiteLayout.module.css";


interface SiteLayoutProps {
    left: ReactNode;
    center: ReactNode;
    right: ReactNode;
}

export default function SiteLayout({left, right, center}: SiteLayoutProps){

    return(
        <div className={styles.container}>
            {/*Side Bar*/}
            <aside className={styles.sidebar}>
                {left}
            </aside>
            {/* Summary Pane*/}
            <main className={styles.summary}>
                {center}
            </main>
            {/* Input Panel*/}
            <section className={styles.input}>
                {right}
            </section>
        </div>
    )
}
/*








 */