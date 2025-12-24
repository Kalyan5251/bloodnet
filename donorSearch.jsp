<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BloodNet - Find Donors</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="static/css/main.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        .gradient-bg {
            background: linear-gradient(135deg, #dc2626 0%, #b91c1c 50%, #991b1b 100%);
        }
        .heart-pulse {
            animation: pulse 2s infinite;
        }
        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
        }
        .donor-card {
            transition: all 0.3s ease;
        }
        .donor-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
        }
        .filter-active {
            background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
            color: white;
        }
        .filter-inactive {
            background: #f3f4f6;
            color: #6b7280;
        }
    </style>
</head>
<body class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="navbar fixed top-0 w-full z-40 transition-all duration-300">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center py-4">
                <div class="flex items-center">
                    <div class="flex items-center space-x-3">
                        <div class="bg-white rounded-full p-2 shadow-lg">
                            <svg class="w-6 h-6 text-red-600 heart-pulse" fill="currentColor" viewBox="0 0 24 24">
                                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                            </svg>
                        </div>
                        <span class="text-xl font-bold text-white">BloodNet</span>
                    </div>
                </div>
                <div class="hidden md:flex items-center space-x-6">
                    <a href="index.jsp" class="text-white hover:text-red-200 transition-colors">Home</a>
                    <a href="register.jsp" class="text-white hover:text-red-200 transition-colors">Register</a>
                    <a href="login.jsp" class="text-white hover:text-red-200 transition-colors">Login</a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="gradient-bg pt-20 pb-16">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center">
                <h1 class="text-4xl font-bold text-white mb-4">Find Blood Donors</h1>
                <p class="text-red-100 text-lg max-w-2xl mx-auto">
                    Connect with verified blood donors in your area. Our AI-powered matching ensures you find the right donor quickly and safely.
                </p>
            </div>
        </div>
    </section>

    <!-- Search Filters -->
    <section class="py-8 bg-white shadow-lg">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
                <!-- Blood Type Filter -->
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">Blood Type</label>
                    <select id="bloodTypeFilter" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500">
                        <option value="all">All Blood Types</option>
                        <option value="A+">A+</option>
                        <option value="A-">A-</option>
                        <option value="B+">B+</option>
                        <option value="B-">B-</option>
                        <option value="AB+">AB+</option>
                        <option value="AB-">AB-</option>
                        <option value="O+">O+</option>
                        <option value="O-">O-</option>
                    </select>
                </div>

                <!-- Distance Filter -->
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">Distance</label>
                    <div class="flex items-center space-x-2">
                        <input type="range" id="distanceRange" min="1" max="50" value="10" 
                               class="flex-1 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer">
                        <span id="distanceValue" class="text-sm font-medium text-gray-700">10 km</span>
                    </div>
                </div>

                <!-- City Filter -->
                <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">City</label>
                    <input type="text" id="cityFilter" placeholder="Enter city name" 
                           class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500">
                </div>

                <!-- Search Button -->
                <div class="flex items-end">
                    <button onclick="searchDonors()" 
                            class="w-full bg-red-600 text-white px-6 py-3 rounded-lg hover:bg-red-700 transition-colors font-semibold">
                        🔍 Search Donors
                    </button>
                </div>
            </div>

            <!-- Quick Filters -->
            <div class="mt-6">
                <div class="flex flex-wrap gap-2">
                    <button onclick="filterByBloodType('all')" class="blood-type-filter filter-active px-4 py-2 rounded-full text-sm font-medium transition-all">
                        All
                    </button>
                    <button onclick="filterByBloodType('O+')" class="blood-type-filter filter-inactive px-4 py-2 rounded-full text-sm font-medium transition-all">
                        O+ (Most Common)
                    </button>
                    <button onclick="filterByBloodType('O-')" class="blood-type-filter filter-inactive px-4 py-2 rounded-full text-sm font-medium transition-all">
                        O- (Universal Donor)
                    </button>
                    <button onclick="filterByBloodType('AB+')" class="blood-type-filter filter-inactive px-4 py-2 rounded-full text-sm font-medium transition-all">
                        AB+ (Universal Recipient)
                    </button>
                    <button onclick="filterByBloodType('AB-')" class="blood-type-filter filter-inactive px-4 py-2 rounded-full text-sm font-medium transition-all">
                        AB- (Rarest)
                    </button>
                </div>
            </div>
        </div>
    </section>

    <!-- Search Results -->
    <section class="py-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <!-- Results Header -->
            <div class="flex justify-between items-center mb-8">
                <div>
                    <h2 class="text-2xl font-bold text-gray-900">Available Donors</h2>
                    <p class="text-gray-600" id="resultsCount">Showing 12 donors in your area</p>
                </div>
                <div class="flex items-center space-x-4">
                    <button onclick="toggleView()" id="viewToggle" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors min-w-[120px] text-center">
                        📍 Map View
                    </button>
                    <select id="sortBy" class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500">
                        <option value="distance">Sort by Distance</option>
                        <option value="availability">Sort by Availability</option>
                        <option value="lastDonation">Sort by Last Donation</option>
                    </select>
                </div>
            </div>

            <!-- Donor Cards Grid -->
            <div id="donorGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <!-- Donor cards will be populated by JavaScript -->
            </div>

            <!-- Load More Button -->
            <div class="text-center mt-12">
                <button onclick="loadMoreDonors()" 
                        class="bg-red-600 text-white px-8 py-3 rounded-lg hover:bg-red-700 transition-colors font-semibold">
                    Load More Donors
                </button>
            </div>
        </div>
    </section>

    <!-- Map Section -->
    <section id="mapSection" class="py-8 bg-white hidden">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <!-- Results Header for Map View -->
            <div class="flex justify-between items-center mb-8">
                <div>
                    <h2 class="text-2xl font-bold text-gray-900">Donor Locations Map</h2>
                    <p class="text-gray-600" id="mapResultsCount">Showing 12 donors in your area</p>
                </div>
                <div class="flex items-center space-x-4">
                    <button onclick="toggleView()" id="mapViewToggle" class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors min-w-[120px] text-center">
                        📋 Grid View
                    </button>
                    <select id="mapSortBy" class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500">
                        <option value="distance">Sort by Distance</option>
                        <option value="availability">Sort by Availability</option>
                        <option value="lastDonation">Sort by Last Donation</option>
                    </select>
                </div>
            </div>
            <div class="bg-gray-200 rounded-xl h-96 mb-6 relative overflow-hidden">
                <div id="map" class="w-full h-full rounded-xl"></div>
                <div class="absolute top-4 right-4 bg-white rounded-lg shadow-lg p-3">
                    <div class="flex items-center space-x-4 text-sm">
                        <div class="flex items-center">
                            <div class="w-3 h-3 bg-red-500 rounded-full mr-2"></div>
                            <span>Available Donors</span>
                        </div>
                        <div class="flex items-center">
                            <div class="w-3 h-3 bg-green-500 rounded-full mr-2"></div>
                            <span>Online Now</span>
                        </div>
                        <div class="flex items-center">
                            <div class="w-3 h-3 bg-yellow-500 rounded-full mr-2"></div>
                            <span>Recently Active</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Emergency Section -->
    <section class="py-12 bg-red-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
            <h2 class="text-3xl font-bold text-gray-900 mb-4">Emergency Blood Request?</h2>
            <p class="text-gray-600 mb-8 max-w-2xl mx-auto">
                If you have an urgent blood requirement, our emergency system will help you find donors within minutes.
            </p>
            <button onclick="requestEmergency()" 
                    class="bg-red-600 text-white px-8 py-4 rounded-lg hover:bg-red-700 transition-colors font-semibold text-lg btn-pulse">
                🚨 Request Emergency Blood
            </button>
        </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-900 text-white py-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
                <div class="col-span-1 md:col-span-2">
                    <div class="flex items-center mb-4">
                        <div class="bg-red-600 rounded-full p-2 mr-3">
                            <svg class="w-6 h-6 text-white" fill="currentColor" viewBox="0 0 24 24">
                                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                            </svg>
                        </div>
                        <span class="text-2xl font-bold">BloodNet</span>
                    </div>
                    <p class="text-gray-400 mb-4">
                        Connecting life-saving blood donors with patients in need through smart technology and compassionate care.
                    </p>
                </div>
                
                <div>
                    <h3 class="text-lg font-semibold mb-4">Quick Links</h3>
                    <ul class="space-y-2">
                        <li><a href="index.jsp" class="text-gray-400 hover:text-white transition-colors">Home</a></li>
                        <li><a href="register.jsp" class="text-gray-400 hover:text-white transition-colors">Register</a></li>
                        <li><a href="login.jsp" class="text-gray-400 hover:text-white transition-colors">Login</a></li>
                        <li><a href="dashboard.jsp" class="text-gray-400 hover:text-white transition-colors">Dashboard</a></li>
                    </ul>
                </div>
                
                <div>
                    <h3 class="text-lg font-semibold mb-4">Contact</h3>
                    <div class="space-y-2 text-gray-400">
                        <p>📞 +1 (555) 123-4567</p>
                        <p>✉️ support@bloodnet.com</p>
                        <p>📍 123 Medical Center, Health City</p>
                    </div>
                </div>
            </div>
            
            <div class="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
                <p>&copy; 2024 BloodNet. Making a difference, one donation at a time.</p>
            </div>
        </div>
    </footer>

    <!-- Scripts -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="static/js/app.js"></script>
    <script src="static/js/chat.js"></script>
    <script src="static/js/aiAssistant.js"></script>
    
    <script>
        // Sample donor data
        const donorData = [
            {
                id: 1,
                name: "Donor****",
                bloodType: "O+",
                distance: "2.4 km",
                lastDonation: "3 months ago",
                availability: "Online",
                city: "New York",
                isOnline: true
            },
            {
                id: 2,
                name: "Donor****",
                bloodType: "A+",
                distance: "1.8 km",
                lastDonation: "2 months ago",
                availability: "Online",
                city: "New York",
                isOnline: true
            },
            {
                id: 3,
                name: "Donor****",
                bloodType: "B-",
                distance: "3.2 km",
                lastDonation: "1 month ago",
                availability: "Offline",
                city: "New York",
                isOnline: false
            },
            {
                id: 4,
                name: "Donor****",
                bloodType: "AB+",
                distance: "4.1 km",
                lastDonation: "4 months ago",
                availability: "Online",
                city: "New York",
                isOnline: true
            },
            {
                id: 5,
                name: "Donor****",
                bloodType: "O-",
                distance: "2.9 km",
                lastDonation: "6 months ago",
                availability: "Online",
                city: "New York",
                isOnline: true
            },
            {
                id: 6,
                name: "Donor****",
                bloodType: "A-",
                distance: "5.3 km",
                lastDonation: "2 weeks ago",
                availability: "Online",
                city: "New York",
                isOnline: true
            }
        ];

        let filteredDonors = [...donorData];
        let currentFilter = 'all';
        let isMapView = false;
        let map;

        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            renderDonorCards();
            setupEventListeners();
            initializeMap();
        });

        function setupEventListeners() {
            // Distance slider
            const distanceSlider = document.getElementById('distanceRange');
            const distanceValue = document.getElementById('distanceValue');
            
            distanceSlider.addEventListener('input', function() {
                distanceValue.textContent = `${this.value} km`;
            });

            // Search input
            const cityFilter = document.getElementById('cityFilter');
            cityFilter.addEventListener('input', debounce(filterDonors, 300));

            // Blood type filter
            const bloodTypeFilter = document.getElementById('bloodTypeFilter');
            bloodTypeFilter.addEventListener('change', filterDonors);

            // Sort dropdown
            const sortBy = document.getElementById('sortBy');
            sortBy.addEventListener('change', sortDonors);
        }

        function renderDonorCards() {
            const donorGrid = document.getElementById('donorGrid');
            const resultsCount = document.getElementById('resultsCount');
            const mapResultsCount = document.getElementById('mapResultsCount');
            
            donorGrid.innerHTML = '';
            
            filteredDonors.forEach(donor => {
                const card = createDonorCard(donor);
                donorGrid.appendChild(card);
            });
            
            const countText = `Showing ${filteredDonors.length} donors in your area`;
            resultsCount.textContent = countText;
            if (mapResultsCount) {
                mapResultsCount.textContent = countText;
            }
        }

        function createDonorCard(donor) {
            const card = document.createElement('div');
            card.className = 'donor-card bg-white rounded-xl shadow-lg p-6';
            
            card.innerHTML = `
                <div class="flex items-start justify-between mb-4">
                    <div>
                        <h3 class="donor-name text-lg font-semibold text-gray-900">${donor.name}</h3>
                        <p class="donor-blood-type text-red-600 font-bold text-xl">${donor.bloodType}</p>
                    </div>
                    <div class="donor-status px-2 py-1 rounded-full text-xs font-semibold ${
                        donor.isOnline ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
                    }">
                        ${donor.availability}
                    </div>
                </div>
                
                <div class="space-y-2 mb-4">
                    <div class="flex items-center text-sm text-gray-600">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        </svg>
                        <span class="donor-location">${donor.distance} • ${donor.city}</span>
                    </div>
                    <div class="flex items-center text-sm text-gray-600">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                        Last donation: ${donor.lastDonation}
                    </div>
                </div>
                
                <div class="flex space-x-3">
                    <button onclick="connectWithDonor(${donor.id})" 
                            class="flex-1 bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors font-semibold">
                        💬 Connect
                    </button>
                    <button onclick="viewDonorProfile(${donor.id})" 
                            class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
                        👤 Profile
                    </button>
                </div>
            `;
            
            return card;
        }

        function filterByBloodType(bloodType) {
            currentFilter = bloodType;
            
            // Update filter buttons
            document.querySelectorAll('.blood-type-filter').forEach(btn => {
                btn.classList.remove('filter-active');
                btn.classList.add('filter-inactive');
            });
            
            event.target.classList.remove('filter-inactive');
            event.target.classList.add('filter-active');
            
            // Filter donors
            if (bloodType === 'all') {
                filteredDonors = [...donorData];
            } else {
                filteredDonors = donorData.filter(donor => donor.bloodType === bloodType);
            }
            
            renderDonorCards();
            if (isMapView && map) {
                addDonorMarkers();
            }
        }

        function filterDonors() {
            const bloodType = document.getElementById('bloodTypeFilter').value;
            const distance = parseInt(document.getElementById('distanceRange').value);
            const city = document.getElementById('cityFilter').value.toLowerCase();
            
            filteredDonors = donorData.filter(donor => {
                const bloodMatch = bloodType === 'all' || donor.bloodType === bloodType;
                const distanceMatch = parseFloat(donor.distance) <= distance;
                const cityMatch = !city || donor.city.toLowerCase().includes(city);
                
                return bloodMatch && distanceMatch && cityMatch;
            });
            
            renderDonorCards();
            if (isMapView && map) {
                addDonorMarkers();
            }
        }

        function sortDonors() {
            const sortBy = document.getElementById('sortBy').value;
            
            filteredDonors.sort((a, b) => {
                switch (sortBy) {
                    case 'distance':
                        return parseFloat(a.distance) - parseFloat(b.distance);
                    case 'availability':
                        return b.isOnline - a.isOnline;
                    case 'lastDonation':
                        return new Date(b.lastDonation) - new Date(a.lastDonation);
                    default:
                        return 0;
                }
            });
            
            renderDonorCards();
            if (isMapView && map) {
                addDonorMarkers();
            }
        }

        function connectWithDonor(donorId) {
            const donor = donorData.find(d => d.id === donorId);
            if (donor) {
                // Open chat interface
                if (window.ChatSystem) {
                    window.ChatSystem.openChat(donorId);
                } else {
                    alert(`Connecting with ${donor.name}...`);
                }
            }
        }

        function viewDonorProfile(donorId) {
            const donor = donorData.find(d => d.id === donorId);
            if (donor) {
                alert(`Viewing profile for ${donor.name} (${donor.bloodType})`);
            }
        }

        function searchDonors() {
            filterDonors();
        }

        function loadMoreDonors() {
            // Simulate loading more donors
            const loadingBtn = event.target;
            const originalText = loadingBtn.textContent;
            loadingBtn.innerHTML = '<div class="loading-spinner mx-auto"></div>';
            loadingBtn.disabled = true;
            
            setTimeout(() => {
                loadingBtn.textContent = originalText;
                loadingBtn.disabled = false;
                alert('More donors loaded!');
            }, 1500);
        }

        function requestEmergency() {
            if (confirm('This will send an emergency alert to all nearby donors. Are you sure?')) {
                alert('Emergency request sent! Donors will be notified immediately.');
            }
        }

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

        function initializeMap() {
            // Initialize map centered on New York
            map = L.map('map').setView([40.7128, -74.0060], 11);
            
            // Add OpenStreetMap tiles
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);
            
            // Add donor markers
            addDonorMarkers();
        }

        function addDonorMarkers() {
            if (!map) return;
            
            // Clear existing markers
            map.eachLayer(layer => {
                if (layer instanceof L.Marker) {
                    map.removeLayer(layer);
                }
            });
            
            // Add markers for each donor
            filteredDonors.forEach(donor => {
                const lat = 40.7128 + (Math.random() - 0.5) * 0.1;
                const lng = -74.0060 + (Math.random() - 0.5) * 0.1;
                
                const markerColor = donor.isOnline ? 'green' : 'red';
                const marker = L.circleMarker([lat, lng], {
                    radius: 8,
                    fillColor: markerColor,
                    color: markerColor,
                    weight: 2,
                    opacity: 1,
                    fillOpacity: 0.8
                }).addTo(map);
                
                marker.bindPopup(`
                    <div class="p-2">
                        <h3 class="font-semibold text-gray-900">${donor.name}</h3>
                        <p class="text-red-600 font-bold">${donor.bloodType}</p>
                        <p class="text-sm text-gray-600">${donor.distance} • ${donor.city}</p>
                        <p class="text-sm text-gray-600">Last donation: ${donor.lastDonation}</p>
                        <div class="mt-2">
                            <button onclick="connectWithDonor(${donor.id})" 
                                    class="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700">
                                Connect
                            </button>
                        </div>
                    </div>
                `);
            });
        }

        function toggleView() {
            const viewToggle = document.getElementById('viewToggle');
            const mapViewToggle = document.getElementById('mapViewToggle');
            const donorGrid = document.getElementById('donorGrid');
            const mapSection = document.getElementById('mapSection');
            const resultsCount = document.getElementById('resultsCount');
            const mapResultsCount = document.getElementById('mapResultsCount');
            
            if (isMapView) {
                // Switch to grid view
                donorGrid.classList.remove('hidden');
                mapSection.classList.add('hidden');
                viewToggle.innerHTML = '📍 Map View';
                isMapView = false;
            } else {
                // Switch to map view
                donorGrid.classList.add('hidden');
                mapSection.classList.remove('hidden');
                mapViewToggle.innerHTML = '📋 Grid View';
                isMapView = true;
                // Update map markers and results count
                if (mapResultsCount) {
                    mapResultsCount.textContent = resultsCount.textContent;
                }
                setTimeout(() => {
                    if (map) {
                        map.invalidateSize();
                        addDonorMarkers();
                    }
                }, 100);
            }
        }
    </script>
</body>
</html>
