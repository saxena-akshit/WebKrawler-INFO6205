import { useState } from "react";
import responseData from "../model/api-response.json";
import "./Results.css";

function Results() {
    const { data } = responseData;
    const [showGraph, setShowGraph] = useState(false);

    const handleGraphToggle = () => {
        setShowGraph((prev) => !prev);
    };

    return (
        <div className="results-container">
            <div className="results-content">
                {/* Left Section: URL List */}
                <div className="list-section">
                    <h2>URLs List</h2>
                    <p>Total URLs Crawled: {responseData.total_urls_crawled}</p>
                    <ul className="url-list">
                        {data.map((item, index) => (
                            <li key={index} className="url-item">
                                <div className="url">{item.url}</div>
                                <div className="details">
                                    <span>Rank: {item.rank}</span>
                                    <span>In-Degree: {item.in_degree}</span>
                                </div>
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Right Section: Graph Button */}
                <div className="graph-section">
                    <button className="graph-button" onClick={handleGraphToggle}>
                        {showGraph ? "Close Graph Visualization" : "Click Here for Graph Visualization"}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Results;
