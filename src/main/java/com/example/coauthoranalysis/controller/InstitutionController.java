package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.model.Institution;
import com.example.coauthoranalysis.repository.InstitutionRepository;
import com.example.coauthoranalysis.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
// InstitutionController.java
@RestController
@RequestMapping("/institutions")
public class InstitutionController {
    private final InstitutionRepository institutionRepository;

    public InstitutionController(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    // 获取机构排名
    @GetMapping("/rankings")
    public List<Institution> getInstitutionRankings(@RequestParam(defaultValue = "10") int limit) {
        return (List<Institution>) institutionRepository.findTopNByOrderByAverageScoreDesc(PageRequest.of(0, limit));
    }
}