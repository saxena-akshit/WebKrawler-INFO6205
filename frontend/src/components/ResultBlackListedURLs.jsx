import "./Results.css";

function ResultBlackListedURLs() {
    const blacklistedUrls = [
        "https://blacklisted-url1.com",
        "https://blacklisted-url2.com",
        "https://malicious-site.com",
        "https://phishing-attack.com",
    ];

    return (
        <div className="blacklist-section">
            <h2>Black-listed URLs</h2>
            <ul className="blacklist-list">
                {blacklistedUrls.map((url, index) => (
                    <li key={index} className="blacklist-item">
                        {url}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default ResultBlackListedURLs;
