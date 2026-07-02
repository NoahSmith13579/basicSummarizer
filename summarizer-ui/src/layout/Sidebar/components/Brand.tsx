import styles from "../Sidebar.module.css";

// TODO: Username div is not needed
export default function Brand() {
  return (
    <div className={styles.brand}>
      <div className={styles.brandContainer}>
        <div className={styles.brandLogo}>
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="3" width="12" height="2" rx="1" fill="#6C63FF" />
            <rect
              x="2"
              y="7"
              width="8"
              height="2"
              rx="1"
              fill="#6C63FF"
              opacity=".6"
            />
            <rect
              x="2"
              y="11"
              width="10"
              height="2"
              rx="1"
              fill="#6C63FF"
              opacity=".4"
            />
          </svg>
        </div>
      </div>
      <div>
        <div className={styles.brandTitle}>Summarizer</div>
        <div className={styles.brandUsername}></div>
      </div>
    </div>
  );
}
