import "./Results.css";

function ResultBlackListedURLs() {
    const blacklistedUrls = [
        "https://www.creativebookmark.com/",
        "https://www.ffupdate.org",
        "https://www.ad-tracker.example",
        "https://www.popup-ads-site.net",
        "https://www.bannerads.org",
        "https://nu.outsystemsenterprise.com/FSD/",
        "https://www.instagram.com/northeastern/",
        "https://www.northeastern.edu/charlotte/",
        "https://geo.northeastern.edu/blog/country/hong-kong/",
        "https://www.tiktok.com/@northeasternu",
        "https://research.northeastern.edu/cognitive-and-brain-health/",
        "https://www.facebook.com",
        "https://www.twitter.com",
        "https://www.instagram.com",
        "https://www.fonts.googleapis.com",
        "https://www.google.com",
        "https://www.youtube.com",

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
