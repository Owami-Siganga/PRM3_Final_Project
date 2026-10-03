package za.ac.cput.communitystore.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import za.ac.cput.communitystore.model.User;
import za.ac.cput.communitystore.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String fullName, String email, String password, String contactNumber) {
        if (email == null || !email.toLowerCase().endsWith(".ac.za")) {
            throw new IllegalArgumentException("Please use a valid South African university email ending in .ac.za");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setContactNumber(contactNumber);

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        LocalDateTime now = LocalDateTime.now();

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new IllegalArgumentException("Account temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setLockedUntil(now.plusMinutes(10));
                user.setFailedLoginAttempts(0);
            }

            userRepository.save(user);
            throw new IllegalArgumentException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return user;
    }
}
