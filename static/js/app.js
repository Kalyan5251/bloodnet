// BloodNet - Main Application JavaScript

// Global variables
let currentUser = null;
let bloodStockData = {
    'A+': { count: 45, urgent: false },
    'A-': { count: 12, urgent: true },
    'B+': { count: 38, urgent: false },
    'B-': { count: 8, urgent: true },
    'AB+': { count: 15, urgent: false },
    'AB-': { count: 3, urgent: true },
    'O+': { count: 67, urgent: false },
    'O-': { count: 5, urgent: true }
};

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
    updateBloodStock();
    startRealTimeUpdates();
});

function initializeApp() {
    // Add loading animation
    showLoading();
    
    // Initialize components
    initializeNavigation();
    initializeBloodStockCards();
    initializeSearchFilters();
    
    // Hide loading after 1 second
    setTimeout(() => {
        hideLoading();
    }, 1000);
}

function setupEventListeners() {
    // Navigation
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', handleNavigation);
    });
    
    // Search functionality
    const searchInput = document.getElementById('donorSearch');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
    
    // Form submissions
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', handleFormSubmit);
    });
    
    // Blood type filters
    const bloodTypeFilters = document.querySelectorAll('.blood-type-filter');
    bloodTypeFilters.forEach(filter => {
        filter.addEventListener('click', handleBloodTypeFilter);
    });
}

function initializeNavigation() {
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.add('navbar-blur');
            } else {
                navbar.classList.remove('navbar-blur');
            }
        });
    }
}

function initializeBloodStockCards() {
    const bloodStockContainer = document.getElementById('bloodStock');
    if (bloodStockContainer) {
        updateBloodStockCards();
    }
}

function updateBloodStockCards() {
    const container = document.getElementById('bloodStock');
    if (!container) return;
    
    container.innerHTML = '';
    
    Object.entries(bloodStockData).forEach(([type, data]) => {
        const card = createBloodStockCard(type, data);
        container.appendChild(card);
    });
}

function createBloodStockCard(type, data) {
    const card = document.createElement('div');
    card.className = `blood-type-card rounded-xl p-4 text-center cursor-pointer ${data.urgent ? 'urgent' : ''}`;
    
    card.innerHTML = `
        <div class="text-2xl font-bold text-red-600 mb-2">${type}</div>
        <div class="text-lg font-semibold ${data.urgent ? 'text-red-700' : 'text-gray-700'}">${data.count}</div>
        <div class="text-sm text-gray-600">units available</div>
        ${data.urgent ? '<div class="text-xs text-red-600 font-semibold mt-1">URGENT</div>' : ''}
    `;
    
    card.addEventListener('click', () => {
        showBloodTypeDetails(type, data);
    });
    
    return card;
}

function showBloodTypeDetails(type, data) {
    // Create modal for blood type details
    const modal = document.createElement('div');
    modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    modal.innerHTML = `
        <div class="bg-white rounded-2xl p-8 max-w-md mx-4">
            <div class="text-center">
                <div class="text-4xl font-bold text-red-600 mb-4">${type}</div>
                <div class="text-2xl font-semibold text-gray-800 mb-2">${data.count} units available</div>
                <div class="text-gray-600 mb-6">${data.urgent ? 'Urgent need - Please donate soon!' : 'Stock levels are stable'}</div>
                <button onclick="this.closest('.fixed').remove()" 
                        class="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-colors">
                    Close
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
}

function initializeSearchFilters() {
    const distanceSlider = document.getElementById('distanceRange');
    if (distanceSlider) {
        distanceSlider.addEventListener('input', updateDistanceDisplay);
    }
}

function updateDistanceDisplay() {
    const slider = document.getElementById('distanceRange');
    const display = document.getElementById('distanceValue');
    if (slider && display) {
        display.textContent = `${slider.value} km`;
    }
}

function handleNavigation(event) {
    event.preventDefault();
    const target = event.target.getAttribute('href');
    
    // Add smooth transition
    document.body.style.opacity = '0.8';
    
    setTimeout(() => {
        window.location.href = target;
    }, 200);
}

function handleSearch(event) {
    const query = event.target.value.toLowerCase();
    const donorCards = document.querySelectorAll('.donor-card');
    
    donorCards.forEach(card => {
        const name = card.querySelector('.donor-name').textContent.toLowerCase();
        const bloodType = card.querySelector('.donor-blood-type').textContent.toLowerCase();
        const location = card.querySelector('.donor-location').textContent.toLowerCase();
        
        if (name.includes(query) || bloodType.includes(query) || location.includes(query)) {
            card.style.display = 'block';
            card.style.animation = 'slideIn 0.3s ease-out';
        } else {
            card.style.display = 'none';
        }
    });
}

function handleBloodTypeFilter(event) {
    const selectedType = event.target.dataset.bloodType;
    const donorCards = document.querySelectorAll('.donor-card');
    
    donorCards.forEach(card => {
        const cardBloodType = card.querySelector('.donor-blood-type').textContent;
        
        if (selectedType === 'all' || cardBloodType === selectedType) {
            card.style.display = 'block';
            card.style.animation = 'slideIn 0.3s ease-out';
        } else {
            card.style.display = 'none';
        }
    });
    
    // Update active filter
    document.querySelectorAll('.blood-type-filter').forEach(filter => {
        filter.classList.remove('bg-red-600', 'text-white');
        filter.classList.add('bg-gray-200', 'text-gray-700');
    });
    
    event.target.classList.remove('bg-gray-200', 'text-gray-700');
    event.target.classList.add('bg-red-600', 'text-white');
}

function handleFormSubmit(event) {
    // Skip forms with custom handlers
    if (event.target.hasAttribute('data-custom-handler')) {
        return;
    }
    
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    
    // Show loading
    const submitBtn = form.querySelector('button[type="submit"]');
    if (submitBtn) {
        const originalText = submitBtn.textContent;
        submitBtn.innerHTML = '<div class="loading-spinner"></div> Processing...';
        submitBtn.disabled = true;
        
        // Simulate form processing
        setTimeout(() => {
            showMessage('Form submitted successfully!', 'success');
            form.reset();
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        }, 2000);
    }
}

function updateBloodStock() {
    // Simulate real-time updates
    setInterval(() => {
        Object.keys(bloodStockData).forEach(type => {
            const change = Math.floor(Math.random() * 3) - 1; // -1, 0, or 1
            bloodStockData[type].count = Math.max(0, bloodStockData[type].count + change);
            
            // Update urgent status
            bloodStockData[type].urgent = bloodStockData[type].count < 10;
        });
        
        updateBloodStockCards();
    }, 30000); // Update every 30 seconds
}

function startRealTimeUpdates() {
    // Update donor availability
    setInterval(() => {
        const donorCards = document.querySelectorAll('.donor-card');
        donorCards.forEach(card => {
            const status = card.querySelector('.donor-status');
            if (status) {
                const isOnline = Math.random() > 0.3; // 70% chance of being online
                status.textContent = isOnline ? 'Online' : 'Offline';
                status.className = `donor-status px-2 py-1 rounded-full text-xs font-semibold ${
                    isOnline ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                }`;
            }
        });
    }, 15000); // Update every 15 seconds
}

function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 message-${type}`;
    messageDiv.innerHTML = `
        <div class="flex items-center text-white">
            <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            ${message}
        </div>
    `;
    
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

function showLoading() {
    const loading = document.createElement('div');
    loading.id = 'loading-overlay';
    loading.className = 'fixed inset-0 bg-white bg-opacity-90 flex items-center justify-center z-50';
    loading.innerHTML = `
        <div class="text-center">
            <div class="loading-spinner mx-auto mb-4"></div>
            <div class="text-red-600 font-semibold">Loading BloodNet...</div>
        </div>
    `;
    document.body.appendChild(loading);
}

function hideLoading() {
    const loading = document.getElementById('loading-overlay');
    if (loading) {
        loading.remove();
    }
}

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Export functions for use in other scripts
window.BloodNet = {
    showMessage,
    updateBloodStock,
    bloodStockData
};
