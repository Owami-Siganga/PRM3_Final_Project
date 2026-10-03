package za.ac.cput.communitystore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.communitystore.model.BulletinPost;
import za.ac.cput.communitystore.repository.BulletinPostRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulletin")
@CrossOrigin
public class BulletinController {

    private final BulletinPostRepository repository;

    public BulletinController(BulletinPostRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<BulletinPost> all() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BulletinPost post) {
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required"));
        }

        if (post.getContent() == null || post.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content is required"));
        }

        return ResponseEntity.ok(repository.save(post));
    }
}
