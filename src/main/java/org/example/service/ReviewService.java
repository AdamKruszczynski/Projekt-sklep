package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.Company;
import org.example.entity.NotificationType;
import org.example.entity.Review;
import org.example.entity.User;
import org.example.exception.InvalidOrderException;
import org.example.repository.CompanyRepository;
import org.example.repository.ReviewRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void addReview(String username, Long companyId, String content, int rating) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Company company = companyRepository.findById(companyId).orElseThrow();

        if (reviewRepository.existsByUserAndCompany(user, company)) {
            throw new InvalidOrderException("Już dodałeś opinię o tej firmie.");
        }

        Review review = Review.builder()
                .content(content)
                .rating(rating)
                .createdAt(LocalDateTime.now())
                .user(user)
                .company(company)
                .build();

        reviewRepository.save(review); // 🟢 Zapisujemy NAJPIERW

//         🔔 Powiadomienia dla właściciela
        User owner = company.getOwner();
            if (owner != null) {
                notificationService.sendNotification(
                        owner.getUsername(),
                        "Nowa opinia o Twojej firmie \"" + company.getName() + "\" została dodana.",
                        NotificationType.NEW_REVIEW
                );
            }


//         🔔 Powiadomienia dla moderatorów
        List<User> moderators = userRepository.findByCompanyAndRole(company, User.Role.MODERATOR);
        for (User moderator : moderators) {
            notificationService.sendNotification(
                    moderator.getUsername(),
                    "Nowa opinia o firmie \"" + company.getName() + "\" została dodana.",
                    NotificationType.NEW_REVIEW
            );
        }
    }


    public List<Review> getReviewsForCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        return reviewRepository.findByCompanyOrderByCreatedAtDesc(company);
    }

    public boolean hasUserReviewedCompany(User user, Company company) {
        return reviewRepository.existsByUserAndCompany(user, company);
    }

    public boolean hasUserReviewedCompany(String username, Long companyId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Company company = companyRepository.findById(companyId).orElseThrow();
        return reviewRepository.existsByUserAndCompany(user, company);
    }

    @Transactional
    public void deleteReviewByUsernameAndCompany(String username, Long companyId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Company company = companyRepository.findById(companyId).orElseThrow();

        Review review = reviewRepository.findByUserAndCompany(user, company)
                .orElseThrow(() -> new IllegalStateException("Opinia nie znaleziona"));

        reviewRepository.delete(review);
    }

    public Optional<Review> getById(Long id) {
        return reviewRepository.findById(id);
    }
}
