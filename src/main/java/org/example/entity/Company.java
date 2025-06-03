package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String description;
    private String phoneNumber;
    private String email;
    private String photo;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Category> categories = new ArrayList<>();

    public Company(String name, String description, String phoneNumber, String email) {
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public Company(String name, String type, String description, String phoneNumber, String email, User owner) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.owner = owner;
    }
}