package org.example.service;

import org.example.entity.Notification;
import org.example.entity.NotificationType;
import org.example.entity.User;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }

    public long countUnread(String username) {
        return notificationRepository.countByUserUsernameAndReadFalse(username);
    }

    public void sendNotification(String username, String message, NotificationType type) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getLatestUnreadNotifications(String username, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return notificationRepository.findTopUnreadByUsername(username, pageable);
    }

    public void markAsRead(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByUserUsernameAndReadFalse(username);
    }

    public List<Notification> getUserUnreadNotifications(String username) {
        return notificationRepository.findByUserUsernameAndReadFalseOrderByCreatedAtDesc(username);
    }

    public void markAllAsRead(String username) {
        List<Notification> unread = notificationRepository.findByUserUsernameAndReadFalse(username);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

}
