<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Chat Application</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    
    <link rel="stylesheet" type="text/css" href="./resources/css/app.css">
    <link rel="stylesheet" href="./resources/plugins/fontawesome/css/all.min.css">
</head>
<body>
    <div class="chat-container">
        <div class="chat-header">
            <h1><i class="fas fa-comment-dots"></i>Chat Application</h1>
        </div>
        
        <div class="chat-main">
            <div class="sidebar">
                <h2><i class="fas fa-user-friends"></i> Online Users</h2>
                <ul class="online-users" id="onlineUsers">
                    <!-- Online users will be dynamically added here -->
                </ul>
            </div>
            
            <div class="chat-content">
                <div class="chat-messages" id="chatbox">
                    <!-- Messages will be dynamically added here -->
                </div>
                
                <div class="chat-input-area">
                    <input type="text" id="message" class="chat-input" placeholder="Type your message...">
                    <button class="send-button" onclick="sendMessage()">
                        <i class="fas fa-paper-plane"></i>
                    </button>
                </div>
            </div>
        </div>
        
        <div class="chat-footer">
            <p id="connectionStatus">Connecting to chat server...</p>
        </div>
    </div>
    
    <div class="notification" id="notification">
        Message sent successfully!
    </div>

    <script>
        const contextPath = "<%=request.getContextPath()%>";
    </script>
    <script type="text/javascript" src="./resources/js/app.js"></script>
</body>
</html>