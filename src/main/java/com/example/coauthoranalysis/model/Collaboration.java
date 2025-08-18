// src/main/java/com/example/coauthoranalysis/entity/CoAuthorshipEdge.java
package com.example.coauthoranalysis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collaboration")
public class Collaboration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // 添加fetch策略
    @JoinColumn(name = "author1_id", referencedColumnName = "id")
    private Author author1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author2_id", referencedColumnName = "id")
    private Author author2;
    private Double weight;
    public void setWeight(double v) {
    }

    // 添加级联配置（可选）
}