package za.ac.cput.communitystore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.communitystore.model.Notification;
import za.ac.cput.communitystore.repository.NotificationRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin
public class CheckoutController {

    private final NotificationRepository notificationRepository;

    public CheckoutController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body) {
        Object userIdValue = body.get("userId");

        if (userIdValue == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please log in before checkout"));
        }

        Long userId = Long.valueOf(userIdValue.toString());

        notificationRepository.save(
                new Notification(userId, "Purchase completed successfully. Demo checkout used; connect PayFast/SnapScan for real payments.")
        );

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Demo checkout successful",
                "paymentMode", "DEMO"
        ));
    }
}
