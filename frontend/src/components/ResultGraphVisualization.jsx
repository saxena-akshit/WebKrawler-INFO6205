import { useState } from "react";
import "./Results.css";

function ResultGraphVisualization() {
    const [showGraph, setShowGraph] = useState(false);

    const handleGraphToggle = () => {
        setShowGraph((prev) => !prev);
    };

    return (
        <div className="graph-section">
            <h2>Graph Visualization</h2>
            <button className="graph-button" onClick={handleGraphToggle}>
                {showGraph ? "Close Graph Visualization" : "Click Here for Graph Visualization"}
            </button>
            {showGraph && <div id="graph-container">[Graph Component Placeholder]</div>}
        </div>
    );
}

export default ResultGraphVisualization;
