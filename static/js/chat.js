// BloodNet - Chat System JavaScript

let chatMessages = [];
let isTyping = false;
let currentChatId = null;

// Initialize chat system
document.addEventListener('DOMContentLoaded', function() {
    initializeChat();
    setupChatEventListeners();
    loadChatHistory();
});

function initializeChat() {
    // Create chat interface if it doesn't exist
    if (!document.getElementById('chatContainer')) {
        createChatInterface();
    }
    
    // Add sample messages
    addSampleMessages();
}

function createChatInterface() {
    const chatHTML = `
        <div id="chatContainer" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 hidden">
            <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 h-96 flex flex-col">
                <!-- Chat Header -->
                <div class="bg-red-600 text-white p-4 rounded-t-2xl flex items-center justify-between">
                    <div class="flex items-center">
                        <div class="w-3 h-3 bg-green-400 rounded-full mr-2"></div>
                        <span class="font-semibold">Anonymous Chat</span>
                    </div>
                    <button onclick="closeChat()" class="text-white hover:text-gray-200">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                    </button>
                </div>
                
                <!-- Chat Messages -->
                <div id="chatMessages" class="flex-1 p-4 overflow-y-auto space-y-3">
                    <!-- Messages will be inserted here -->
                </div>
                
                <!-- Typing Indicator -->
                <div id="typingIndicator" class="px-4 py-2 hidden">
                    <div class="flex items-center text-gray-500">
                        <div class="typing-indicator mr-2">●</div>
                        <div class="typing-indicator mr-2" style="animation-delay: 0.2s">●</div>
                        <div class="typing-indicator" style="animation-delay: 0.4s">●</div>
                        <span class="ml-2 text-sm">Donor is typing...</span>
                    </div>
                </div>
                
                <!-- Chat Input -->
                <div class="p-4 border-t border-gray-200">
                    <div class="flex space-x-2">
                        <input type="text" id="chatInput" placeholder="Type your message..." 
                               class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500">
                        <button onclick="sendMessage()" class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                            </svg>
                        </button>
                    </div>
                    
                    <!-- Privacy Notice -->
                    <div class="mt-2 text-xs text-gray-500 text-center">
                        🔒 Privacy-first & secure • Personal info shared only after mutual consent
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', chatHTML);
}

function setupChatEventListeners() {
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
        
        chatInput.addEventListener('input', function() {
            if (!isTyping && this.value.length > 0) {
                showTypingIndicator();
            }
        });
    }
}

function addSampleMessages() {
    const sampleMessages = [
        {
            id: 1,
            sender: 'donor',
            message: 'Hello! I saw your blood request. I have O+ blood type.',
            timestamp: new Date(Date.now() - 300000), // 5 minutes ago
            delivered: true
        },
        {
            id: 2,
            sender: 'user',
            message: 'Thank you for responding! Are you available to donate today?',
            timestamp: new Date(Date.now() - 240000), // 4 minutes ago
            delivered: true
        },
        {
            id: 3,
            sender: 'donor',
            message: 'Yes, I can come to the hospital in the next hour. What time works for you?',
            timestamp: new Date(Date.now() - 180000), // 3 minutes ago
            delivered: true
        }
    ];
    
    chatMessages = sampleMessages;
    renderMessages();
}

function renderMessages() {
    const chatMessagesContainer = document.getElementById('chatMessages');
    if (!chatMessagesContainer) return;
    
    chatMessagesContainer.innerHTML = '';
    
    chatMessages.forEach(message => {
        const messageElement = createMessageElement(message);
        chatMessagesContainer.appendChild(messageElement);
    });
    
    // Scroll to bottom
    chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
}

function createMessageElement(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`;
    
    const bubbleClass = message.sender === 'user' 
        ? 'bg-red-600 text-white' 
        : 'bg-gray-100 text-gray-800';
    
    const deliveredIcon = message.delivered 
        ? '<svg class="w-3 h-3 ml-1" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>'
        : '';
    
    messageDiv.innerHTML = `
        <div class="max-w-xs lg:max-w-md">
            <div class="${bubbleClass} rounded-2xl px-4 py-2 chat-bubble">
                <div class="text-sm">${message.message}</div>
                <div class="flex items-center justify-end mt-1 text-xs opacity-70">
                    <span>${formatTime(message.timestamp)}</span>
                    ${deliveredIcon}
                </div>
            </div>
        </div>
    `;
    
    return messageDiv;
}

function sendMessage() {
    const chatInput = document.getElementById('chatInput');
    const message = chatInput.value.trim();
    
    if (!message) return;
    
    // Add user message
    const userMessage = {
        id: Date.now(),
        sender: 'user',
        message: message,
        timestamp: new Date(),
        delivered: true
    };
    
    chatMessages.push(userMessage);
    chatInput.value = '';
    
    // Clear typing indicator
    hideTypingIndicator();
    
    // Render messages
    renderMessages();
    
    // Simulate donor response
    setTimeout(() => {
        simulateDonorResponse();
    }, 2000);
}

function simulateDonorResponse() {
    const responses = [
        "I understand. Let me check my schedule.",
        "That works for me. I'll be there soon.",
        "Thank you for the information. I'm on my way.",
        "Perfect! I'll bring my ID and medical records.",
        "I'm available now. What's the exact location?",
        "I've donated before, so I know the process.",
        "Is there anything specific I need to know?",
        "I'm ready to help. Every life matters!"
    ];
    
    const randomResponse = responses[Math.floor(Math.random() * responses.length)];
    
    const donorMessage = {
        id: Date.now(),
        sender: 'donor',
        message: randomResponse,
        timestamp: new Date(),
        delivered: true
    };
    
    chatMessages.push(donorMessage);
    renderMessages();
}

function showTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) {
        typingIndicator.classList.remove('hidden');
        isTyping = true;
        
        // Hide after 3 seconds
        setTimeout(() => {
            hideTypingIndicator();
        }, 3000);
    }
}

function hideTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) {
        typingIndicator.classList.add('hidden');
        isTyping = false;
    }
}

function openChat(donorId) {
    currentChatId = donorId;
    const chatContainer = document.getElementById('chatContainer');
    if (chatContainer) {
        chatContainer.classList.remove('hidden');
        document.getElementById('chatInput').focus();
    }
}

function closeChat() {
    const chatContainer = document.getElementById('chatContainer');
    if (chatContainer) {
        chatContainer.classList.add('hidden');
    }
    currentChatId = null;
}

function loadChatHistory() {
    // In a real application, this would load from a database
    // For now, we'll use the sample messages
    console.log('Chat history loaded');
}

function formatTime(timestamp) {
    const now = new Date();
    const messageTime = new Date(timestamp);
    const diffInMinutes = Math.floor((now - messageTime) / 60000);
    
    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}h ago`;
    
    return messageTime.toLocaleDateString();
}

// Export functions for global use
window.ChatSystem = {
    openChat,
    closeChat,
    sendMessage
};
