package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "company_id"}),
        name = "reviews"
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String content;

    private int rating;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @ManyToOne
    private Company company;
}
