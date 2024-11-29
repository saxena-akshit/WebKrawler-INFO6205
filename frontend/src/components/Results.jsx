import { useState } from "react";
import ResultCrawledURLs from "./ResultCrawledURLs";
import "./Results.css";
import ResultGraphVisualization from "./ResultGraphVisualization";
import ResultPageRanking from "./ResultPageRanking";
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
                    className={`tab-button ${activeTab === 2 ? "active" : ""}`}
                    onClick={() => setActiveTab(2)}
                    disabled
                >
                    Graph Visualization
                </button>
                <button
                    className={`tab-button ${activeTab === 3 ? "active" : ""}`}
                    onClick={() => setActiveTab(3)}
                >
                    Page Ranking
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
                {activeTab === 2 && <ResultGraphVisualization />}
                {activeTab === 3 && <ResultPageRanking />}
                {activeTab === 4 && <ResultBlackListedURLs />}

            </div>
        </div>
    );
}

export default Results;
