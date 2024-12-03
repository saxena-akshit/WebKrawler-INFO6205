import { useState } from "react";
import ResultCrawledURLs from "./ResultCrawledURLs";
import "./Results.css";

import ResultBlackListedURLs from "./ResultBlackListedURLs";

function Results() {
    const [activeTab, setActiveTab] = useState(1); // Track the active tab

    return (
        <div className="results-container">
            {/* Tab Navigation */}
            <div className="tabs">
                <button
                    className={`tab-button ${activeTab === 1 ? "active" : ""}`}
                    onClick={() => setActiveTab(1)}
                >
                    Crawled URLs
                </button>

                <button
                    className={`tab-button ${activeTab === 4 ? "active" : ""}`}
                    onClick={() => setActiveTab(4)}
                >
                    Black-listed URLs
                </button>
            </div>
            <div className="tab-content">
                {activeTab === 1 && <ResultCrawledURLs />}
                {activeTab === 4 && <ResultBlackListedURLs />}

            </div>
        </div>
    );
}

export default Results;
