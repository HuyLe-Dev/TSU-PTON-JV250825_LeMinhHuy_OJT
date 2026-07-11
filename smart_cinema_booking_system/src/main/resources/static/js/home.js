/* ===== SMART CINEMA - HOME PAGE JS ===== */

document.addEventListener('DOMContentLoaded', function () {
  initNavbarScroll();
  initNavActiveTab();
  initSearchBox();
  initCarousels();
  initTrailerModal();
  initHeroSwitch();
  initMovieCardClick();
});

/* ===== NAVBAR SCROLL EFFECT ===== */
function initNavbarScroll() {
  var navbar = document.querySelector('.navbar-cinema');
  if (!navbar) return;

  window.addEventListener('scroll', function () {
    if (window.scrollY > 50) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
  });
}

/* ===== NAV ACTIVE TAB — click + scroll detection ===== */
function initNavActiveTab() {
  var navLinks = document.querySelectorAll('.nav-links a');
  if (navLinks.length === 0) return;

  // Map nav links to their target sections
  var sectionMap = [];
  navLinks.forEach(function (link) {
    var href = link.getAttribute('href');
    if (href && href.startsWith('#') && href.length > 1) {
      var target = document.querySelector(href);
      if (target) sectionMap.push({ link: link, section: target });
    } else {
      // "Trang chủ" links to # or top
      sectionMap.push({ link: link, section: null });
    }
  });

  // Click: chuyển active ngay
  navLinks.forEach(function (link) {
    link.addEventListener('click', function () {
      navLinks.forEach(function (l) { l.classList.remove('active'); });
      this.classList.add('active');
    });
  });

  // Scroll: auto-detect active section
  window.addEventListener('scroll', function () {
    var scrollPos = window.scrollY + 200;
    var activeLink = navLinks[0]; // default = Trang chủ

    sectionMap.forEach(function (item) {
      if (item.section && item.section.offsetTop <= scrollPos) {
        activeLink = item.link;
      }
    });

    navLinks.forEach(function (l) { l.classList.remove('active'); });
    activeLink.classList.add('active');
  });
}

/* ===== SEARCH BOX ===== */
function initSearchBox() {
  var searchBox = document.querySelector('.search-box');
  var searchInput = document.querySelector('.search-box input');
  var searchIcon = document.querySelector('.search-icon');
  var dropdown = document.querySelector('.search-results-dropdown');

  if (!searchBox || !searchInput || !searchIcon) return;

  // Click icon -> mở search box
  searchIcon.addEventListener('click', function (e) {
    e.stopPropagation();
    searchBox.classList.toggle('active');
    if (searchBox.classList.contains('active')) {
      searchInput.focus();
    } else {
      searchInput.value = '';
      if (dropdown) dropdown.classList.remove('visible');
    }
  });

  // Khi focus vào input -> tự động mở
  searchInput.addEventListener('focus', function () {
    searchBox.classList.add('active');
    // Nếu đã có text, hiện kết quả
    if (this.value.trim().length > 0) {
      filterSearch(this.value);
    }
  });

  // Gõ text -> filter kết quả
  searchInput.addEventListener('input', function () {
    filterSearch(this.value);
  });

  function filterSearch(value) {
    if (!dropdown) return;
    var query = value.trim().toLowerCase();
    var items = dropdown.querySelectorAll('.search-result-item');
    var hasVisible = false;

    items.forEach(function (item) {
      var title = (item.getAttribute('data-title') || '').toLowerCase();
      if (query.length > 0 && title.includes(query)) {
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
  }

  // Click bên ngoài -> đóng
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
    var carousel = section.querySelector('.movie-carousel');
    var prevBtn = section.querySelector('.btn-prev');
    var nextBtn = section.querySelector('.btn-next');

    if (!carousel) return;

    var scrollAmount = 220;

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
  var modal = document.getElementById('trailerModal');
  if (!modal) return;

  var iframe = modal.querySelector('iframe');
  var closeBtn = modal.querySelector('.close-modal');

  // Mở trailer từ bất kỳ element nào có data-trailer
  document.addEventListener('click', function (e) {
    var trigger = e.target.closest('[data-trailer]');
    if (trigger) {
      e.preventDefault();
      e.stopPropagation();
      var trailerUrl = trigger.getAttribute('data-trailer');
      if (trailerUrl && iframe) {
        iframe.src = convertToEmbed(trailerUrl);
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
      }
    }
  });

  // Đóng modal
  if (closeBtn) {
    closeBtn.addEventListener('click', closeTrailer);
  }
  modal.addEventListener('click', function (e) {
    if (e.target === modal) closeTrailer();
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && modal.classList.contains('active')) closeTrailer();
  });

  function closeTrailer() {
    modal.classList.remove('active');
    if (iframe) iframe.src = '';
    document.body.style.overflow = '';
  }
}

/* Convert YouTube URL to embed */
function convertToEmbed(url) {
  if (!url) return '';
  if (url.includes('/embed/')) return url;
  var match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([a-zA-Z0-9_-]{11})/);
  if (match) {
    return 'https://www.youtube.com/embed/' + match[1] + '?autoplay=1&rel=0';
  }
  return url;
}

/* ===== HERO MOVIE SWITCH (hover card -> đổi hero) ===== */
function initHeroSwitch() {
  var heroCards = document.querySelectorAll('.movie-card[data-hero]');
  var heroBackdrop = document.querySelector('.hero-backdrop');
  var heroTitle = document.querySelector('.hero-title');
  var heroDesc = document.querySelector('.hero-description');
  var heroMeta = document.querySelector('.hero-meta');
  var heroTrailerBtn = document.querySelector('.btn-trailer');

  if (heroCards.length === 0) return;

  heroCards.forEach(function (card) {
    card.addEventListener('mouseenter', function () {
      var backdrop = this.getAttribute('data-hero-backdrop');
      var title = this.getAttribute('data-hero-title');
      var desc = this.getAttribute('data-hero-desc');
      var trailer = this.getAttribute('data-hero-trailer');
      var genres = this.getAttribute('data-hero-genres');
      var year = this.getAttribute('data-hero-year');
      var duration = this.getAttribute('data-hero-duration');
      var age = this.getAttribute('data-hero-age');

      if (heroBackdrop && backdrop) {
        heroBackdrop.style.backgroundImage = 'url(' + backdrop + ')';
      }
      if (heroTitle && title) heroTitle.textContent = title;
      if (heroDesc && desc) heroDesc.textContent = desc;
      if (heroTrailerBtn && trailer) {
        heroTrailerBtn.setAttribute('data-trailer', trailer);
        heroTrailerBtn.style.display = '';
      }

      if (heroMeta) {
        heroMeta.innerHTML = '';
        if (genres) addMetaSpan(heroMeta, 'genre-tag', genres);
        if (year) addMetaSpan(heroMeta, 'year', year);
        if (duration) addMetaSpan(heroMeta, 'duration', duration + ' phút');
        if (age) addMetaSpan(heroMeta, 'age-badge', age);
      }
    });
  });

  function addMetaSpan(parent, className, text) {
    var span = document.createElement('span');
    span.className = className;
    span.textContent = text;
    parent.appendChild(span);
  }
}

/* ===== MOVIE CARD CLICK → mở trailer ===== */
function initMovieCardClick() {
  var cards = document.querySelectorAll('.movie-card');
  if (cards.length === 0) return;

  cards.forEach(function (card) {
    card.addEventListener('click', function (e) {
      // Nếu click vào play-icon có data-trailer → để trailer modal xử lý
      if (e.target.closest('[data-trailer]')) return;

      // Lấy trailer URL từ card data
      var trailer = this.getAttribute('data-hero-trailer');
      if (trailer) {
        // Mở trailer modal
        var modal = document.getElementById('trailerModal');
        var iframe = modal ? modal.querySelector('iframe') : null;
        if (modal && iframe) {
          iframe.src = convertToEmbed(trailer);
          modal.classList.add('active');
          document.body.style.overflow = 'hidden';
        }
      } else {
        // Scroll lên hero section để xem chi tiết
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  });

  // ===== TOAST LOGIC =====
  const toasts = document.querySelectorAll('.toast');
  toasts.forEach(toast => {
    // Auto dismiss after 5 seconds
    const timeout = setTimeout(() => {
      dismissToast(toast);
    }, 5000);

    // Close button click
    const closeBtn = toast.querySelector('.toast-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        clearTimeout(timeout);
        dismissToast(toast);
      });
    }
  });

  function dismissToast(toast) {
    toast.classList.add('hiding');
    toast.addEventListener('animationend', () => {
      toast.remove();
    });
  }
};
