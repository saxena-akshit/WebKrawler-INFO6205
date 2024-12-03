import "./Results.css";

function ResultBlackListedURLs() {
    const blacklistedUrls = [

    ];

    return (
        <div className="blacklist-section">
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
