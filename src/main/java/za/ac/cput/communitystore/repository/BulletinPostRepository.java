package za.ac.cput.communitystore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.communitystore.model.BulletinPost;

import java.util.List;

public interface BulletinPostRepository extends JpaRepository<BulletinPost, Long> {
    List<BulletinPost> findAllByOrderByCreatedAtDesc();
}
