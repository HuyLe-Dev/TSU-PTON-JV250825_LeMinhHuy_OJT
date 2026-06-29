/* ===== SMART CINEMA - HOME PAGE JS ===== */

document.addEventListener('DOMContentLoaded', function () {
    initNavbarScroll();
    initSearchBox();
    initCarousels();
    initTrailerModal();
    initHeroSwitch();
});

/* ===== NAVBAR SCROLL EFFECT ===== */
function initNavbarScroll() {
    const navbar = document.querySelector('.navbar-cinema');
    if (!navbar) return;

    window.addEventListener('scroll', function () {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
}

/* ===== SEARCH BOX ===== */
function initSearchBox() {
    const searchBox = document.querySelector('.search-box');
    const searchInput = document.querySelector('.search-box input');
    const searchIcon = document.querySelector('.search-icon');
    const dropdown = document.querySelector('.search-results-dropdown');

    if (!searchBox || !searchInput || !searchIcon) return;

    // Toggle search expand
    searchIcon.addEventListener('click', function () {
        searchBox.classList.toggle('active');
        if (searchBox.classList.contains('active')) {
            searchInput.focus();
        } else {
            searchInput.value = '';
            if (dropdown) dropdown.classList.remove('visible');
        }
    });

    // Filter search results
    if (dropdown) {
        searchInput.addEventListener('input', function () {
            const query = this.value.trim().toLowerCase();
            const items = dropdown.querySelectorAll('.search-result-item');
            let hasVisible = false;

            items.forEach(function (item) {
                const title = item.getAttribute('data-title') || '';
                if (query.length > 0 && title.toLowerCase().includes(query)) {
                    item.style.display = 'flex';
                    hasVisible = true;
                } else {
                    item.style.display = 'none';
                }
            });

            if (hasVisible && query.length > 0) {
                dropdown.classList.add('visible');
            } else {
                dropdown.classList.remove('visible');
            }
        });
    }

    // Close search on click outside
    document.addEventListener('click', function (e) {
        if (!searchBox.contains(e.target)) {
            searchBox.classList.remove('active');
            searchInput.value = '';
            if (dropdown) dropdown.classList.remove('visible');
        }
    });
}

/* ===== CAROUSEL NAVIGATION ===== */
function initCarousels() {
    document.querySelectorAll('[data-carousel]').forEach(function (section) {
        const carousel = section.querySelector('.movie-carousel');
        const prevBtn = section.querySelector('.btn-prev');
        const nextBtn = section.querySelector('.btn-next');

        if (!carousel) return;

        var scrollAmount = 220; // card width + gap

        if (nextBtn) {
            nextBtn.addEventListener('click', function () {
                carousel.scrollBy({ left: scrollAmount * 3, behavior: 'smooth' });
            });
        }

        if (prevBtn) {
            prevBtn.addEventListener('click', function () {
                carousel.scrollBy({ left: -scrollAmount * 3, behavior: 'smooth' });
            });
        }
    });
}

/* ===== TRAILER MODAL ===== */
function initTrailerModal() {
    const modal = document.getElementById('trailerModal');
    if (!modal) return;

    const iframe = modal.querySelector('iframe');
    const closeBtn = modal.querySelector('.close-modal');

    // Open trailer buttons
    document.querySelectorAll('[data-trailer]').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            var trailerUrl = this.getAttribute('data-trailer');
            if (trailerUrl && iframe) {
                var embedUrl = convertToEmbed(trailerUrl);
                iframe.src = embedUrl;
                modal.classList.add('active');
                document.body.style.overflow = 'hidden';
            }
        });
    });

    // Close modal
    if (closeBtn) {
        closeBtn.addEventListener('click', closeTrailer);
    }

    modal.addEventListener('click', function (e) {
        if (e.target === modal) {
            closeTrailer();
        }
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeTrailer();
        }
    });

    function closeTrailer() {
        modal.classList.remove('active');
        if (iframe) iframe.src = '';
        document.body.style.overflow = '';
    }
}

/* Convert YouTube URL to embed URL */
function convertToEmbed(url) {
    if (!url) return '';
    // Already an embed URL
    if (url.includes('/embed/')) return url;
    // Standard youtube.com/watch?v= format
    var match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([a-zA-Z0-9_-]{11})/);
    if (match) {
        return 'https://www.youtube.com/embed/' + match[1] + '?autoplay=1&rel=0';
    }
    return url;
}

/* ===== HERO MOVIE SWITCH ===== */
function initHeroSwitch() {
    const heroCards = document.querySelectorAll('.movie-card[data-hero]');
    const heroBackdrop = document.querySelector('.hero-backdrop');
    const heroTitle = document.querySelector('.hero-title');
    const heroDesc = document.querySelector('.hero-description');
    const heroBadge = document.querySelector('.hero-badge');
    const heroMeta = document.querySelector('.hero-meta');
    const heroTrailerBtn = document.querySelector('.hero-buttons [data-trailer]');

    if (heroCards.length === 0) return;

    heroCards.forEach(function (card) {
        card.addEventListener('mouseenter', function () {
            var poster = this.getAttribute('data-hero-poster');
            var title = this.getAttribute('data-hero-title');
            var desc = this.getAttribute('data-hero-desc');
            var trailer = this.getAttribute('data-hero-trailer');
            var genres = this.getAttribute('data-hero-genres');
            var year = this.getAttribute('data-hero-year');
            var duration = this.getAttribute('data-hero-duration');
            var age = this.getAttribute('data-hero-age');

            if (heroBackdrop && poster) {
                heroBackdrop.style.backgroundImage = 'url(' + poster + ')';
            }
            if (heroTitle && title) heroTitle.textContent = title;
            if (heroDesc && desc) heroDesc.textContent = desc;
            if (heroTrailerBtn && trailer) heroTrailerBtn.setAttribute('data-trailer', trailer);

            if (heroMeta) {
                heroMeta.innerHTML = '';
                if (genres) {
                    var span = document.createElement('span');
                    span.className = 'genre-tag';
                    span.textContent = genres;
                    heroMeta.appendChild(span);
                }
                if (year) {
                    var span = document.createElement('span');
                    span.className = 'year';
                    span.textContent = year;
                    heroMeta.appendChild(span);
                }
                if (duration) {
                    var span = document.createElement('span');
                    span.className = 'duration';
                    span.textContent = duration + ' phut';
                    heroMeta.appendChild(span);
                }
                if (age) {
                    var span = document.createElement('span');
                    span.className = 'age-badge';
                    span.textContent = age;
                    heroMeta.appendChild(span);
                }
            }
        });
    });
}
