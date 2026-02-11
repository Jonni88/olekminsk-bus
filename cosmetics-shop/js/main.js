// Main JavaScript
document.addEventListener('DOMContentLoaded', () => {
    // Mobile menu toggle
    window.toggleMenu = function() {
        const menu = document.getElementById('mobileMenu');
        menu.classList.toggle('active');
    };

    // Render featured products on homepage
    const featuredContainer = document.getElementById('featuredProducts');
    if (featuredContainer) {
        renderProducts(products.slice(0, 4), featuredContainer);
    }

    // Update cart count on load
    cart.updateUI();
});

// Render products grid
function renderProducts(productsToRender, container) {
    if (!container) return;
    
    container.innerHTML = productsToRender.map(product => `
        <div class="product-card">
            <div class="product-image">
                <img src="${product.image}" alt="${product.name}" class="product-img">
                ${product.badge ? `<span class="product-badge ${product.badge}">${getBadgeText(product.badge)}</span>` : ''}
            </div>
            <div class="product-info">
                <div class="product-brand">${product.brand}</div>
                <div class="product-name">${product.name}</div>
                <div class="product-footer">
                    <div class="product-price">
                        ${product.price.toLocaleString()} ₽
                        ${product.oldPrice ? `<span class="old">${product.oldPrice.toLocaleString()} ₽</span>` : ''}
                    </div>
                    <button class="add-to-cart" onclick="addToCart(${product.id})" title="Добавить в корзину">+</button>
                </div>
            </div>
        </div>
    `).join('');
}

// Get badge text
function getBadgeText(badge) {
    const badges = {
        'new': 'Новинка',
        'hit': 'Хит',
        'sale': 'Скидка'
    };
    return badges[badge] || badge;
}

// Add to cart function
function addToCart(productId) {
    const product = products.find(p => p.id === productId);
    if (product) {
        cart.add(product);
    }
}

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Header scroll effect
let lastScroll = 0;
const header = document.querySelector('.header');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;
    
    if (currentScroll > 100) {
        header.style.boxShadow = '0 4px 30px rgba(0,0,0,0.1)';
    } else {
        header.style.boxShadow = '0 2px 20px rgba(0,0,0,0.05)';
    }
    
    lastScroll = currentScroll;
});
