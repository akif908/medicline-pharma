package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig — enables STOMP messaging over WebSocket.
 *
 * How it works:
 *  - Client connects to ws://localhost:8080/ws (SockJS fallback enabled)
 *  - Client subscribes to topics like /topic/chat/{consultationId}
 *    or /user/{userId}/queue/notifications
 *  - Server broadcasts to those topics via SimpMessagingTemplate
 *
 * Networking model:
 *  - Each WebSocket connection is a persistent, bi-directional TCP socket
 *    (not HTTP request-response). Spring manages the connection lifecycle
 *    on dedicated WebSocket session threads.
 *  - The in-memory message broker routes messages between sessions
 *    without external infrastructure (Redis/RabbitMQ).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure the in-memory STOMP message broker.
     * /topic  — broadcast destinations (one → many, e.g., chat rooms)
     * /queue  — point-to-point destinations (one → one, e.g., user notifications)
     * /app    — prefix that routes messages to @MessageMapping handler methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register the WebSocket handshake endpoint.
     * Clients connect here to upgrade from HTTP to WebSocket protocol.
     * SockJS provides transparent fallback for browsers that block WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for dev
                .withSockJS();                 // Enables SockJS fallback
    }
}
