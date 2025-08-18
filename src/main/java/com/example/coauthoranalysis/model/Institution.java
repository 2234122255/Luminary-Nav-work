package com.example.coauthoranalysis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Institution.java
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "institution")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)  // Increased from default 255 to 1000
    private String name;

    private Integer paperCount;
    private Integer totalCitations;
    private Double averageScore;
    private Integer authorCount; // 作者数（关键字段）
}