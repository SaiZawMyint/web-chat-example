const socket = new WebSocket("ws://" + location.host + contextPath + "/chat");
const chatbox = document.getElementById('chatbox');
const messageInput = document.getElementById('message');
const notification = document.getElementById('notification');
const connectionStatus = document.getElementById('connectionStatus');
const onlineUsers = document.getElementById('onlineUsers');
let username = '';

// Function to get current time in HH:MM format
function getCurrentTime() {
	const now = new Date();
	return now.getHours().toString().padStart(2, '0') + ':' +
		now.getMinutes().toString().padStart(2, '0');
}

// Function to add a new message to the chat
function addMessage(sender, text, isSent = false, isSystem = false) {
	const messageDiv = document.createElement('div');

	if (isSystem) {
		messageDiv.classList.add('system-message');
		messageDiv.textContent = text;
	} else {
		messageDiv.classList.add('message');
		messageDiv.classList.add(isSent ? 'sent' : 'received');

		messageDiv.innerHTML = `
                    <div class="message-text">${text}</div>
                    <div class="message-info">
                        <span class="message-sender">${sender}</span>
                        <span class="message-time">${getCurrentTime()}</span>
                    </div>
                `;
	}

	chatbox.appendChild(messageDiv);

	// Scroll to bottom
	chatbox.scrollTop = chatbox.scrollHeight;
}

// Function to update online users list
function updateOnlineUsers(userList) {
	onlineUsers.innerHTML = '';
	userList.forEach(user => {
		const li = document.createElement('li');
		li.textContent = user;
		onlineUsers.appendChild(li);
	});
}

// Function to show notification
function showNotification(message) {
	notification.textContent = message;
	notification.classList.add('show');
	setTimeout(() => {
		notification.classList.remove('show');
	}, 2000);
}

// Function to send a message
function sendMessage() {
	const message = messageInput.value.trim();
	if (message !== "") {
		// Create JSON message
		const messageData = {
			type: "chat",
			content: message
		};

		// Send message via WebSocket
		socket.send(JSON.stringify(messageData));

		// Add message to chat as sent (local echo)
		addMessage("You", message, true);

		// Show notification
		showNotification("Message sent!");

		// Clear input
		messageInput.value = "";
	}
}

// WebSocket event handlers
socket.onopen = function() {
	connectionStatus.textContent = "Connected to chat server";
};

socket.onmessage = function(event) {
	try {
		const data = JSON.parse(event.data);

		switch (data.type) {
			case "chat":
				// Add received message (not from self)
				addMessage(data.sender, data.content);
				break;
			case "system":
				addMessage("System", data.content, false, true);
				break;
			case "userlist":
				updateOnlineUsers(data.users);
				const userCount = data.users.length;
				connectionStatus.textContent = `Connected to chat server • ${userCount} online user${userCount !== 1 ? 's' : ''}`;
				break;
		}
	} catch (e) {
		console.error("Error parsing message:", e);
		addMessage("System", event.data, false, true);
	}
};

socket.onerror = function(error) {
	console.error("WebSocket Error:", error);
	connectionStatus.textContent = "Connection error - attempting to reconnect...";
	showNotification("Connection error!");
};

socket.onclose = function() {
	connectionStatus.textContent = "Disconnected from chat server";
	showNotification("Connection closed!");
};

// Send message when Enter key is pressed
messageInput.addEventListener('keypress', function(e) {
	if (e.key === 'Enter') {
		sendMessage();
	}
});

// Focus input on load
messageInput.focus();