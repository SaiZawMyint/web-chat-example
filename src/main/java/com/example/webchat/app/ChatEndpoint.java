package com.example.webchat.app;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for real-time chat functionality.
 * Handles user connections, messaging, disconnections, and user management.
 * 
 * <p>Maintains:
 * <ul>
 *   <li>Active WebSocket sessions</li>
 *   <li>User mappings (session to username)</li>
 *   <li>Automatic username generation</li>
 * </ul>
 * 
 * <p>Implements:
 * <ul>
 *   <li>Join/leave notifications</li>
 *   <li>Real-time messaging</li>
 *   <li>Online user tracking</li>
 *   <li>Prevention of message echoing to sender</li>
 * </ul>
 * 
 * @author SZM
 */
@ServerEndpoint("/chat")
public class ChatEndpoint {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<Session, String> users = Collections.synchronizedMap(new HashMap<>());
    private static int nextUserId = 1;

    /**
     * Handles new WebSocket connection.
     * - Assigns username
     * - Sends welcome message
     * - Notifies other users
     * - Updates online user list
     * 
     * @param session The WebSocket session of the connecting user
     */
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        String username = "User" + nextUserId++;
        users.put(session, username);

        // Send welcome message to new user
        sendMessageToSession(session, createSystemMessage("Welcome to the chat, " + username + "!"));

        // Notify others about new user
        broadcast(createUserJoinMessage(username), session);

        // Update user list for all clients
        broadcastOnlineUsers();
    }

    /**
     * Handles incoming messages from clients.
     * - Parses JSON message
     * - Processes chat messages
     * - Broadcasts to other users (excluding sender)
     * 
     * @param message The raw JSON message string from client
     * @param session The WebSocket session of the sending user
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();
            String type = jsonMessage.getString("type");

            if ("chat".equals(type)) {
                String content = jsonMessage.getString("content");
                String username = users.get(session);
                // Broadcast to others EXCLUDING sender
                broadcast(createChatMessage(username, content), session);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Handles WebSocket connection closure.
     * - Removes user from tracking
     * - Notifies other users
     * - Updates online user list
     * 
     * @param session The WebSocket session of the disconnecting user
     */
    @OnClose
    public void onClose(Session session) {
        String username = users.get(session);
        if (username != null) {
            sessions.remove(session);
            users.remove(session);

            // Notify others about user leaving
            broadcast(createUserLeaveMessage(username), null);

            // Update user list for all clients
            broadcastOnlineUsers();
        }
    }

    /**
     * Broadcasts updated online user list to all connected clients.
     * Formats user list as JSON message with type "userlist".
     */
    private void broadcastOnlineUsers() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        synchronized (users) {
            users.values().forEach(arrayBuilder::add);
        }

        String userListMessage = Json.createObjectBuilder()
                .add("type", "userlist")
                .add("users", arrayBuilder)
                .build()
                .toString();

        broadcast(userListMessage, null);
    }

    /**
     * Creates JSON-formatted chat message.
     * 
     * @param username The sender's username
     * @param content The message content
     * @return JSON string with message data
     */
    private String createChatMessage(String username, String content) {
        return Json.createObjectBuilder()
                .add("type", "chat")
                .add("sender", username)
                .add("content", content)
                .add("timestamp", System.currentTimeMillis())
                .build()
                .toString();
    }

    /**
     * Creates JSON-formatted user join notification.
     * 
     * @param username The username of the new user
     * @return JSON string with system message
     */
    private String createUserJoinMessage(String username) {
        return Json.createObjectBuilder()
                .add("type", "system")
                .add("content", username + " joined the chat")
                .build()
                .toString();
    }

    /**
     * Creates JSON-formatted user leave notification.
     * 
     * @param username The username of the departing user
     * @return JSON string with system message
     */
    private String createUserLeaveMessage(String username) {
        return Json.createObjectBuilder()
                .add("type", "system")
                .add("content", username + " left the chat")
                .build()
                .toString();
    }

    /**
     * Creates JSON-formatted system message.
     * 
     * @param content The system message content
     * @return JSON string with system message
     */
    private String createSystemMessage(String content) {
        return Json.createObjectBuilder()
                .add("type", "system")
                .add("content", content)
                .build()
                .toString();
    }

    /**
     * Broadcasts a message to all connected sessions except the specified sender.
     * 
     * @param message The message to broadcast
     * @param sender The session to exclude from broadcasting (null to include all)
     */
    private void broadcast(String message, Session sender) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen() && (sender == null || !session.equals(sender))) {
                    sendMessageToSession(session, message);
                }
            }
        }
    }

    /**
     * Sends a message to a specific WebSocket session.
     * 
     * @param session The target WebSocket session
     * @param message The message to send
     */
    private void sendMessageToSession(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
        }
    }
}