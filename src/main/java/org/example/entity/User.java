package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    private String email;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Company company;

    public enum Role {
        USER,
        OWNER,
        ADMIN,
        MODERATOR
    }
}