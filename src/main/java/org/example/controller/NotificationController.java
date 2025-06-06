package org.example.controller;

import org.example.entity.Notification;
import org.example.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        String username = principal.getName();
        model.addAttribute("notifications", notificationService.getUserNotifications(username));
        notificationService.markAllAsRead(username);
        return "notification/list"; // utworzysz ten widok za chwilę
    }

    @ModelAttribute("unreadCount")
    public long unreadCount(Principal principal) {
        if (principal != null) {
            return notificationService.countUnread(principal.getName());
        }
        return 0;
    }

    @GetMapping("/latest")
    @ResponseBody
    public List<Notification> getLatestNotifications(Principal principal) {
        return notificationService.getLatestUnreadNotifications(principal.getName(), 3);
    }

    @PostMapping("/mark-read")
    @ResponseBody
    public void markNotificationsAsRead(@RequestBody List<Long> notificationIds) {
        notificationService.markAsRead(notificationIds);
    }
}
