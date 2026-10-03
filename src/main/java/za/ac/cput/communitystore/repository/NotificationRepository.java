package za.ac.cput.communitystore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.communitystore.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
