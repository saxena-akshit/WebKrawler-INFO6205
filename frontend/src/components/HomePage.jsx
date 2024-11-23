import { useState, useEffect } from 'react';
import '../App.css';
import { FaSearch } from 'react-icons/fa';

function HomePage() {
    const [isPopupVisible, setIsPopupVisible] = useState(false);

    const handleSearchClick = () => {
        setIsPopupVisible(true);
    };

    const handleOutsideClick = (event) => {
        if (event.target.className === 'popup') {
            setIsPopupVisible(false);
        }
    };

    useEffect(() => {
        if (isPopupVisible) {
            document.addEventListener('click', handleOutsideClick);
        } else {
            document.removeEventListener('click', handleOutsideClick);
        }

        return () => {
            document.removeEventListener('click', handleOutsideClick);
        };
    }, [isPopupVisible]);

    return (
        <div className="App">
            <h1>WebKrawler</h1>
            <div className="search-bar">
                <input type="text" placeholder="Search..." />
                <button onClick={handleSearchClick}>
                    <FaSearch />
                </button>
            </div>
            {isPopupVisible && (
                <div className="popup">
                    <div className="popup-content">Loading...</div>
                </div>
            )}
        </div>
    );
}

export default HomePage;