package za.ac.cput.communitystore.controller;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.communitystore.model.Notification;
import za.ac.cput.communitystore.model.Product;
import za.ac.cput.communitystore.repository.NotificationRepository;
import za.ac.cput.communitystore.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;

    public ProductController(ProductRepository productRepository, NotificationRepository notificationRepository) {
        this.productRepository = productRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        if (product.getTitle() == null || product.getTitle().isBlank() || product.getTitle().length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Title is required and must be 100 characters or less"));
        }

        if (product.getDescription() == null || product.getDescription().length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "Description is required and must be 500 characters or less"));
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Price must be greater than 0"));
        }

        Product saved = productRepository.save(product);

        if (saved.getSellerId() != null) {
            notificationRepository.save(new Notification(saved.getSellerId(), "Your listing '" + saved.getTitle() + "' was created successfully."));
        }

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product request) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setTitle(request.getTitle());
                    product.setDescription(request.getDescription());
                    product.setPrice(request.getPrice());
                    product.setCategory(request.getCategory());
                    product.setLocation(request.getLocation());
                    product.setImageUrl(request.getImageUrl());

                    Product saved = productRepository.save(product);

                    if (saved.getSellerId() != null) {
                        notificationRepository.save(new Notification(saved.getSellerId(), "Your listing '" + saved.getTitle() + "' was updated."));
                    }

                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long sellerId) {
        return productRepository.findById(id)
                .map(product -> {
                    if (!sellerId.equals(product.getSellerId())) {
                        return ResponseEntity.status(403).body(Map.of("message", "You can only delete your own listing"));
                    }

                    productRepository.delete(product);
                    return ResponseEntity.ok(Map.of("message", "Product deleted"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
