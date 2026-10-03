package za.ac.cput.communitystore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.communitystore.model.Notification;
import za.ac.cput.communitystore.repository.NotificationRepository;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/user/{userId}")
    public List<Notification> forUser(@PathVariable Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        return repository.findById(id)
                .map(notification -> {
                    notification.setReadStatus(true);
                    return ResponseEntity.ok(repository.save(notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
