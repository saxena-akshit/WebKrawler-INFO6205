
import responseData from "../model/api-response.json";
import "./Results.css";

function ResultCrawledURLs() {
    const { data, total_urls_crawled } = responseData;

    return (
        <div className="list-section">
            <p>Total URLs Crawled: {total_urls_crawled}</p>
            <div className="scrollable-list">
                <ul className="url-list">
                    {data.map((item, index) => (
                        <li key={index} className="url-item">
                            <div className="url">{item.url}</div>
                            <div className="details">
                                <span><b>Rank: {item.rank}</b></span>
                                <span><b>In-Degree: {item.in_degree}</b></span>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default ResultCrawledURLs;
