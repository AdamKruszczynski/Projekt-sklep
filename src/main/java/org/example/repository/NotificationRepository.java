package org.example.repository;

import org.example.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);

    long countByUserUsernameAndReadFalse(String username);

    @Query("SELECT n FROM Notification n WHERE n.user.username = :username AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findTopUnreadByUsername(@Param("username") String username, Pageable pageable);
}