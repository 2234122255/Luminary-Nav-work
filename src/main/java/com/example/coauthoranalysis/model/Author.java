package com.example.coauthoranalysis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "AUTHOR")
@NoArgsConstructor  // 必须添加
@AllArgsConstructor // 可选
public class Author {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ORG", length = 1000)
    private String org;

    @Column(name = "PAPER_COUNT")
    private Integer paperCount;

    @Column(name = "TOTAL_CITATIONS")
    private Integer totalCitations;

    @Column(name = "H_INDEX")
    private Integer hIndex;

    @Column(name = "HARMONIC_MEAN")
    private Double harmonicMean;

    @Column(name = "PAGERANK_SCORE")
    private Double pagerankScore;

    @Column(name = "COLLABORATOR_COUNT")
    private Integer collaboratorCount;

    @Column(name = "BETWEENNESS_CENTRALITY")
    private Double betweennessCentrality;

    @Column(name = "TOTAL_SCORE")
    private Double totalScore;
}