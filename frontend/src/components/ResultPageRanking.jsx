import responseData from "../model/api-response.json";
import "./Results.css";

function ResultPageRanking() {
    const sortedData = [...responseData.data].sort((a, b) => b.in_degree - a.in_degree);

    return (
        <div className="ranking-section">
            <h2>Page Ranking</h2>
            <ul className="ranking-list">
                {sortedData.map((item, index) => (
                    <li key={index} className="ranking-item">
                        <div className="url">{item.url}</div>
                        <div className="details">
                            <span>Rank: {item.rank}</span>
                            <span>In-Degree: {item.in_degree}</span>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default ResultPageRanking;