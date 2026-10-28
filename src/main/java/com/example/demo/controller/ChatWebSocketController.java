package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;



/**
 * ChatWebSocketController — handles real-time STOMP/WebSocket messaging.
 *
 * Socket networking:
 *  - Clients connect to ws://localhost:8080/ws using SockJS + STOMP client.
 *  - Messages sent to /app/chat/{id} are routed here by Spring's STOMP broker.
 *  - The saved message is then broadcast to /topic/chat/{id} so ALL subscribers
 *    (doctor + patient in that consultation) receive it instantly — no polling needed.
 *  - Approval notifications are pushed to /user/{userId}/queue/notifications
 *    which delivers only to the specific user's socket session.
 *
 * This replaces the 3-second REST polling loop in dashboard.html for chat messages.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConsultationService consultationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Receives a chat message over WebSocket and broadcasts it to the consultation room.
     *
     * Client sends to: /app/chat/{consultationId}
     * All subscribers of /topic/chat/{consultationId} receive the saved message.
     */
    @MessageMapping("/chat/{consultationId}")
    public void handleChatMessage(
            @DestinationVariable Long consultationId,
            @Payload ChatMessagePayload payload) {

        System.out.printf("[WS-%s] Message in consultation %d from sender %d%n",
                Thread.currentThread().getName(), consultationId, payload.getSenderId());

        // Persist the message using the existing service (reuses REST logic)
        Message saved = consultationService.sendMessage(
                consultationId,
                payload.getSenderId(),
                payload.getContent(),
                payload.getImageUrl()
        );

        // Broadcast to /topic/chat/{consultationId} — doctor + patient both receive it
        messagingTemplate.convertAndSend(
                "/topic/chat/" + consultationId,
                saved
        );
    }

    /**
     * Pushes a real-time notification to a specific user via their private WebSocket queue.
     * Called programmatically from other services (e.g., PrescriptionRequestController)
     * when an admin approves/rejects a request.
     *
     * Destination: /user/{userId}/queue/notifications
     */
    public void pushNotificationToUser(Long userId, String message) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // Persist to DB (for users who missed the push while offline)
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        notificationRepository.save(n);

        // Push over WebSocket to the connected session (if online)
        NotificationPayload payload = new NotificationPayload(message);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload
        );
        System.out.printf("[WS-%s] Notification pushed to user %d: %s%n",
                Thread.currentThread().getName(), userId, message);
    }

    // ── Inner DTOs ──────────────────────────────────────────────────────────

    public static class ChatMessagePayload {
        private Long senderId;
        private String content;
        private String imageUrl;

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class NotificationPayload {
        private final String message;
        private final String timestamp = java.time.LocalDateTime.now().toString();

        public NotificationPayload(String message) { this.message = message; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }
}
