package za.ac.cput.communitystore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.communitystore.model.Review;
import za.ac.cput.communitystore.repository.ReviewRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/seller/{sellerId}")
    public List<Review> bySeller(@PathVariable Long sellerId) {
        return reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating must be between 1 and 5"));
        }

        if (review.getComment() != null && review.getComment().length() > 300) {
            return ResponseEntity.badRequest().body(Map.of("message", "Review must be 300 characters or less"));
        }

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
