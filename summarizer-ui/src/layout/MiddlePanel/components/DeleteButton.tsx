import { useState } from "react";
import styles from "./SummaryCard.module.css";

export function DeleteButton({ onClick }: { onClick: () => void }) {
    const [confirming, setConfirming] = useState(false);

    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();

        if (!confirming) {
            setConfirming(true);
            setTimeout(() => setConfirming(false), 2500);
        } else {
            onClick();
        }
    };

    return (
        <button
            className={styles.deleteButton}
            style={{
                background: confirming ? "#E5444414" : "transparent",
                border: `1px solid ${confirming ? "#E5444480" : "#252836"}`,
                color: confirming ? "#E54444" : "#8B8FA8",
            }}
            onClick={handleClick}
        >
            {confirming ? "Confirm?" : "Delete"}
        </button>
    );
}
