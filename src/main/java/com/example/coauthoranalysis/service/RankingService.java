package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RankingService {
    Page<Author> getLeaderAuthors(Pageable pageable);
    Page<Author> getRisingStarAuthors(Pageable pageable);
    Page<Institution> getInstitutionRankings(Pageable pageable);
}