package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SavedCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // np. "Ulubione zamówienie"

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "savedCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedCartItem> items = new ArrayList<>();
}
