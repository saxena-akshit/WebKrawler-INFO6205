import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';
import { FaSearch } from 'react-icons/fa';

function HomePage() {
    const [searchQuery, setSearchQuery] = useState('');
    const navigate = useNavigate();

    const handleSearchClick = () => {
        if (searchQuery.trim() !== '') {
            navigate('/results', { state: { query: searchQuery } });
        }
    };

    return (
        <div className="App">
            <h1>WebKrawler</h1>
            <div className="search-bar">
                <input
                    type="text"
                    placeholder="Search a URL..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
                <button onClick={handleSearchClick}>
                    <FaSearch />
                </button>
            </div>
        </div>
    );
}

export default HomePage;
