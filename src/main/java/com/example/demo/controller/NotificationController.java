package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Notification> getNotifications(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @DeleteMapping
    public void clearAll(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Notification> notifs = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(notifs);
    }
}
