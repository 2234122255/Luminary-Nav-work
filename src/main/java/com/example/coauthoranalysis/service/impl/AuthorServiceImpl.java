package com.example.coauthoranalysis.service.impl;

import com.example.coauthoranalysis.exception.ResourceNotFoundException;
import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Author> getLeaderAuthors(Pageable pageable) {
        return authorRepository.findTopNByOrderByTotalScoreDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Author> getRisingStarAuthors(Pageable pageable) {
        return authorRepository.findTopNByOrderByHarmonicMeanDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Author getAuthorById(String id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> searchAuthors(String query) {
        return authorRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> getAuthorsByIds(List<String> ids) {
        return authorRepository.findByIds(ids);
    }
    @Transactional(readOnly = true)
    @Override
    public Page<Author> searchAuthors(String query, Pageable pageable) {
        return authorRepository.searchAuthors(query, pageable);
    }
}