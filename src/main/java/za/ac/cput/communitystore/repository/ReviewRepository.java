package za.ac.cput.communitystore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.communitystore.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
