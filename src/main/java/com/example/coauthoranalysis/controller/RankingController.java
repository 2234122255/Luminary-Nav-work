package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Institution;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.InstitutionRepository;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final AuthorRepository authorRepository;
    private final InstitutionRepository institutionRepository;

    public RankingController(AuthorRepository authorRepository, InstitutionRepository institutionRepository) {
        this.authorRepository = authorRepository;
        this.institutionRepository = institutionRepository;
    }

    @GetMapping("/leaders")
    public Page<Author> getLeaders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {

        // 如果有搜索关键词，执行搜索逻辑
        if (name != null && !name.isEmpty()) {
            List<Author> searchResults = authorRepository.findByNameContainingIgnoreCase(name);
            // 由于findByNameContainingIgnoreCase返回的是List，需要手动包装成Page
            int start = Math.min(page * size, searchResults.size());
            int end = Math.min(start + size, searchResults.size());
            List<Author> pageContent = searchResults.subList(start, end);

            return new PageImpl<>(pageContent, PageRequest.of(page, size), searchResults.size());
        }

        // 没有搜索关键词时，执行原有逻辑
        return authorRepository.findAll(
                PageRequest.of(page, size, Sort.by("totalScore").descending())
        );
    }

    // 只返回前10名，不分页
    @GetMapping("/top10")
    public Page<Author> getTop10Leaders() {
        List<Author> top10 = authorRepository.findTop10ByOrderByTotalScoreDesc();
        // 包装为Page对象（页码0，每页10条，总条数10）
        return new PageImpl<>(top10, PageRequest.of(0, 10), top10.size());
    }

    // 新增：获取机构排名榜单
    @GetMapping("/institutions")
    public Page<Institution> getInstitutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return institutionRepository.findAllByOrderByAverageScoreDesc(
                PageRequest.of(page, size)
        );
    }

    // 新增：获取机构排名前10
    @GetMapping("/institutions/top10")
    public Page<Institution> getTop10Institutions() {
        List<Institution> top10 = institutionRepository.findTop10ByOrderByAverageScoreDesc();
        return new PageImpl<>(top10, PageRequest.of(0, 10), top10.size());
    }
}