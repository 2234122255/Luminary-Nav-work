package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/leaders")
    public Page<Author> getLeaderAuthors(Pageable pageable) {
        return authorService.getLeaderAuthors(pageable);
    }

    @GetMapping("/rising-stars")
    public Page<Author> getRisingStarAuthors(Pageable pageable) {
        return authorService.getRisingStarAuthors(pageable);
    }

    @GetMapping("/{id}")
    public Author getAuthorById(@PathVariable String id) {
        return authorService.getAuthorById(id);
    }

    @GetMapping("/search")
    public List<Author> searchAuthors(@RequestParam String query) {
        return authorService.searchAuthors(query);
    }
}