package com.example.coauthoranalysis.service.impl;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Institution;
import com.example.coauthoranalysis.service.AuthorService;
import com.example.coauthoranalysis.service.InstitutionService;
import com.example.coauthoranalysis.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final AuthorService authorService;
    private final InstitutionService institutionService;

    @Override
    public Page<Author> getLeaderAuthors(Pageable pageable) {
        return authorService.getLeaderAuthors(pageable);
    }

    @Override
    public Page<Author> getRisingStarAuthors(Pageable pageable) {
        return authorService.getRisingStarAuthors(pageable);
    }

    @Override
    public Page<Institution> getInstitutionRankings(Pageable pageable) {
        return institutionService.getInstitutionRankings(pageable);
    }
}