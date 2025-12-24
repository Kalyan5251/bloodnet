// BloodNet - AI Assistant JavaScript

let aiAssistantOpen = false;
let conversationHistory = [];

// AI responses based on common queries
const aiResponses = {
    'nearest blood bank': {
        response: "I can help you find the nearest blood bank! Based on your location, here are the closest options:\n\n🏥 City General Hospital - 2.3 km\n🏥 Red Cross Blood Center - 4.1 km\n🏥 Central Medical Center - 5.7 km\n\nWould you like directions to any of these locations?",
        suggestions: ['Get directions', 'Check availability', 'Contact hospital']
    },
    'eligibility': {
        response: "Great question! Here are the general eligibility criteria for blood donation:\n\n✅ Age: 18-65 years\n✅ Weight: At least 50 kg\n✅ Hemoglobin: 12.5 g/dL or higher\n✅ No recent illness or medication\n✅ No tattoos in last 3 months\n\n⚠️ Please consult with a medical professional for specific health conditions.",
        suggestions: ['Check my eligibility', 'Find donation center', 'Schedule appointment']
    },
    'register': {
        response: "I'd be happy to help you register as a donor! Here's how to get started:\n\n1️⃣ Click 'Register as Donor' on our homepage\n2️⃣ Fill out the registration form\n3️⃣ Verify your information\n4️⃣ Complete health screening\n\nRegistration takes about 5 minutes and helps save lives!",
        suggestions: ['Start registration', 'Learn more', 'Contact support']
    },
    'blood type': {
        response: "Blood types are crucial for matching donors and recipients:\n\n🩸 **Universal Donor**: O- (can donate to anyone)\n🩸 **Universal Recipient**: AB+ (can receive from anyone)\n🩸 **Most Common**: O+ (38% of population)\n🩸 **Rarest**: AB- (1% of population)\n\nYour blood type is determined by genetics and affects compatibility.",
        suggestions: ['Check compatibility', 'Find donors', 'Learn more']
    },
    'emergency': {
        response: "🚨 **EMERGENCY BLOOD REQUEST** 🚨\n\nIf this is a life-threatening emergency:\n\n1️⃣ Call emergency services (108/911)\n2️⃣ Contact nearest hospital immediately\n3️⃣ Use our 'Find Donor' feature for urgent matching\n4️⃣ Post in our emergency channel\n\nEvery second counts in emergencies!",
        suggestions: ['Find urgent donors', 'Contact hospital', 'Emergency protocols']
    },
    'default': {
        response: "I'm here to help with blood donation and emergency blood requests! I can assist with:\n\n🔍 Finding nearby blood banks\n✅ Checking donation eligibility\n📝 Registration guidance\n🩸 Blood type information\n🚨 Emergency procedures\n\nWhat would you like to know?",
        suggestions: ['Find blood bank', 'Check eligibility', 'Register as donor', 'Emergency help']
    }
};

// Initialize AI Assistant
document.addEventListener('DOMContentLoaded', function() {
    createAIAssistant();
    setupAIEventListeners();
});

function createAIAssistant() {
    const assistantHTML = `
        <!-- Floating AI Assistant Button -->
        <div id="aiAssistantBtn" class="floating-assistant">
            <button onclick="toggleAIAssistant()" 
                    class="bg-red-600 hover:bg-red-700 text-white p-4 rounded-full shadow-lg transition-all duration-300 hover:scale-110">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                </svg>
            </button>
        </div>

        <!-- AI Assistant Chat Interface -->
        <div id="aiAssistantChat" class="fixed bottom-24 right-4 w-80 h-96 bg-white rounded-2xl shadow-2xl z-50 hidden flex flex-col">
            <!-- Chat Header -->
            <div class="bg-red-600 text-white p-4 rounded-t-2xl flex items-center justify-between">
                <div class="flex items-center">
                    <div class="w-3 h-3 bg-green-400 rounded-full mr-2"></div>
                    <span class="font-semibold">BloodNet AI Assistant</span>
                </div>
                <button onclick="toggleAIAssistant()" class="text-white hover:text-gray-200">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </div>
            
            <!-- Chat Messages -->
            <div id="aiChatMessages" class="flex-1 p-4 overflow-y-auto space-y-3">
                <div class="flex justify-start">
                    <div class="bg-gray-100 rounded-2xl px-4 py-2 max-w-xs">
                        <div class="text-sm text-gray-800">Hello! I'm your BloodNet AI Assistant. How can I help you today?</div>
                        <div class="text-xs text-gray-500 mt-1">Just now</div>
                    </div>
                </div>
            </div>
            
            <!-- Quick Suggestions -->
            <div id="aiSuggestions" class="px-4 py-2 border-t border-gray-200">
                <div class="flex flex-wrap gap-2">
                    <button onclick="sendAIQuery('nearest blood bank')" 
                            class="bg-red-100 text-red-700 px-3 py-1 rounded-full text-xs hover:bg-red-200 transition-colors">
                        Find Blood Bank
                    </button>
                    <button onclick="sendAIQuery('eligibility')" 
                            class="bg-red-100 text-red-700 px-3 py-1 rounded-full text-xs hover:bg-red-200 transition-colors">
                        Check Eligibility
                    </button>
                    <button onclick="sendAIQuery('register')" 
                            class="bg-red-100 text-red-700 px-3 py-1 rounded-full text-xs hover:bg-red-200 transition-colors">
                        How to Register
                    </button>
                </div>
            </div>
            
            <!-- Chat Input -->
            <div class="p-4 border-t border-gray-200">
                <div class="flex space-x-2">
                    <input type="text" id="aiChatInput" placeholder="Ask me anything..." 
                           class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 text-sm">
                    <button onclick="sendAIMessage()" class="bg-red-600 text-white px-3 py-2 rounded-lg hover:bg-red-700 transition-colors">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', assistantHTML);
}

function setupAIEventListeners() {
    const aiChatInput = document.getElementById('aiChatInput');
    if (aiChatInput) {
        aiChatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendAIMessage();
            }
        });
    }
}

function toggleAIAssistant() {
    const chatInterface = document.getElementById('aiAssistantChat');
    const assistantBtn = document.getElementById('aiAssistantBtn');
    
    if (aiAssistantOpen) {
        chatInterface.classList.add('hidden');
        assistantBtn.classList.remove('hidden');
        aiAssistantOpen = false;
    } else {
        chatInterface.classList.remove('hidden');
        assistantBtn.classList.add('hidden');
        aiAssistantOpen = true;
        document.getElementById('aiChatInput').focus();
    }
}

function sendAIMessage() {
    const input = document.getElementById('aiChatInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    // Add user message
    addAIMessage(message, 'user');
    input.value = '';
    
    // Process query and get response
    setTimeout(() => {
        const response = processAIQuery(message);
        addAIMessage(response.response, 'ai');
        updateAISuggestions(response.suggestions);
    }, 1000);
}

function sendAIQuery(query) {
    const input = document.getElementById('aiChatInput');
    input.value = query;
    sendAIMessage();
}

function processAIQuery(query) {
    const lowerQuery = query.toLowerCase();
    
    // Check for specific keywords
    for (const [keyword, response] of Object.entries(aiResponses)) {
        if (lowerQuery.includes(keyword)) {
            return response;
        }
    }
    
    // Check for emergency keywords
    if (lowerQuery.includes('emergency') || lowerQuery.includes('urgent') || lowerQuery.includes('help')) {
        return aiResponses['emergency'];
    }
    
    // Default response
    return aiResponses['default'];
}

function addAIMessage(message, sender) {
    const chatMessages = document.getElementById('aiChatMessages');
    const messageDiv = document.createElement('div');
    
    if (sender === 'user') {
        messageDiv.className = 'flex justify-end';
        messageDiv.innerHTML = `
            <div class="bg-red-600 text-white rounded-2xl px-4 py-2 max-w-xs">
                <div class="text-sm">${message}</div>
                <div class="text-xs opacity-70 mt-1">Just now</div>
            </div>
        `;
    } else {
        messageDiv.className = 'flex justify-start';
        messageDiv.innerHTML = `
            <div class="bg-gray-100 rounded-2xl px-4 py-2 max-w-xs">
                <div class="text-sm text-gray-800 whitespace-pre-line">${message}</div>
                <div class="text-xs text-gray-500 mt-1">AI Assistant</div>
            </div>
        `;
    }
    
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
    
    // Add to conversation history
    conversationHistory.push({ sender, message, timestamp: new Date() });
}

function updateAISuggestions(suggestions) {
    const suggestionsContainer = document.getElementById('aiSuggestions');
    if (!suggestionsContainer || !suggestions) return;
    
    suggestionsContainer.innerHTML = `
        <div class="flex flex-wrap gap-2">
            ${suggestions.map(suggestion => `
                <button onclick="sendAIQuery('${suggestion.toLowerCase()}')" 
                        class="bg-red-100 text-red-700 px-3 py-1 rounded-full text-xs hover:bg-red-200 transition-colors">
                    ${suggestion}
                </button>
            `).join('')}
        </div>
    `;
}

// Auto-suggestions based on page context
function updateContextualSuggestions() {
    const currentPage = window.location.pathname;
    let suggestions = [];
    
    if (currentPage.includes('register')) {
        suggestions = ['Check eligibility', 'Find donation center', 'Registration process'];
    } else if (currentPage.includes('search')) {
        suggestions = ['Find blood bank', 'Check availability', 'Contact donor'];
    } else if (currentPage.includes('dashboard')) {
        suggestions = ['View statistics', 'Manage requests', 'Update inventory'];
    } else {
        suggestions = ['Find blood bank', 'Check eligibility', 'Register as donor', 'Emergency help'];
    }
    
    updateAISuggestions(suggestions);
}

// Export functions for global use
window.AIAssistant = {
    toggleAIAssistant,
    sendAIQuery,
    updateContextualSuggestions
};
