package com.connorng.ReUzit.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "category")
    @JsonIgnoreProperties("category")
    private Set<Listing> listings;

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }

}
